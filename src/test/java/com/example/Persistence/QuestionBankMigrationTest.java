package com.example.Persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.example.Database.RowFormats;
import com.example.Service.RoundService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QuestionBankMigrationTest {
    @Autowired Environment environment;
    private DriverManagerDataSource source;
    private JdbcClient admin;
    private JdbcClient jdbc;
    private String schema;
    private QuestionRepository questions;

    @BeforeEach
    void versionFourBank() {
        source = new DriverManagerDataSource(environment.getRequiredProperty("spring.datasource.url"),
                environment.getProperty("spring.datasource.username"), environment.getProperty("spring.datasource.password"));
        admin = JdbcClient.create(source);
        schema = "bank_" + UUID.randomUUID().toString().replace("-", "");
        admin.sql("create schema \"" + schema + "\"").update();
        source.setSchema(schema);
        Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("4").load().migrate();
        jdbc = JdbcClient.create(source);
        questions = new QuestionRepository(jdbc);
    }

    @AfterEach
    void removeSchema() {
        source.setSchema(null);
        admin.sql("drop schema \"" + schema + "\" cascade").update();
    }

    private void upgrade() {
        Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load().migrate();
    }

    private List<Map<String, Object>> questionRecords() {
        return jdbc.sql("select * from questions order by id").query().listOfRows();
    }

    private List<Map<String, Object>> categories() {
        return jdbc.sql("select * from categories order by id").query().listOfRows();
    }

    private static List<List<String>> tuples(List<String[]> rows) {
        return rows.stream().map(Arrays::asList).toList();
    }

    private void assertCanonical() {
        assertThat(questions.count()).isEqualTo(105);
        assertThat(tuples(questions.export())).containsExactlyInAnyOrderElementsOf(tuples(ShippedQuestions.rows()));
        assertThat(jdbc.sql("select count(*) from questions where text in (?, ?)")
                .params("Which gas do plants absorb from the atmosphere?", "Which gas do humans exhale when they breathe out?")
                .query(Integer.class).single()).isZero();
    }

    @Test
    void intactBankExpandsAndRepeatMigrationChangesNothing() {
        upgrade();
        assertCanonical();
        var before = questionRecords();
        var beforeCategories = categories();
        upgrade();
        assertThat(questionRecords()).isEqualTo(before);
        assertThat(categories()).isEqualTo(beforeCategories);
    }

    @Test
    void freshMigrationMatchesCanonicalBank() {
        source.setSchema(null);
        admin.sql("drop schema \"" + schema + "\" cascade").update();
        admin.sql("create schema \"" + schema + "\"").update();
        source.setSchema(schema);
        upgrade();
        assertCanonical();
    }

    @ParameterizedTest
    @ValueSource(strings = {"custom", "empty", "option", "category", "difficulty", "missingDuplicate"})
    void intentionalBanksStayUnchanged(String kind) {
        switch (kind) {
            case "custom", "empty" -> {
                jdbc.sql("delete from questions").update();
                if (kind.equals("custom")) {
                    questions.replaceAll(List.of(QuestionRepository.HEADER,
                            new String[]{"Custom?", "A", "B", "C", "D", "A", "Custom", "HARD"}));
                }
            }
            case "option" -> jdbc.sql("update questions set option_1 = 'Custom London' where id = 1").update();
            case "category" -> jdbc.sql("update questions set category_id = (select id from categories where name = 'Science') where id = 1").update();
            case "difficulty" -> jdbc.sql("update questions set difficulty = 'HARD' where id = 1").update();
            case "missingDuplicate" -> {
                jdbc.sql("delete from questions where id = 1").update();
                jdbc.sql("""
                        insert into questions (text, option_1, option_2, option_3, option_4, answer, category_id, difficulty)
                        select text, option_1, option_2, option_3, option_4, answer, category_id, difficulty from questions where id = 2
                        """).update();
            }
            default -> throw new IllegalArgumentException(kind);
        }
        var before = questionRecords();
        var beforeCategories = categories();
        upgrade();
        assertThat(questionRecords()).isEqualTo(before);
        assertThat(categories()).isEqualTo(beforeCategories);
    }

    @Test
    void mixedBankKeepsCustomRowsAndDoesNotDuplicateAnExistingCanonicalRow() {
        var custom = new String[]{"What is the capital of France?", "Lyon", "Rome", "Berlin", "Paris", "Paris", "Custom", "HARD"};
        var existing = ShippedQuestions.rows().get(1);
        var bank = new java.util.ArrayList<>(questions.export());
        bank.add(custom);
        bank.add(existing);
        questions.replaceAll(bank);
        var customBefore = jdbc.sql("select * from questions where text = ? and option_1 = 'Lyon'")
                .param(custom[0]).query().singleRow();
        upgrade();
        assertThat(questions.count()).isEqualTo(106);
        assertThat(jdbc.sql("select * from questions where id = ?").param(customBefore.get("id"))
                .query().singleRow()).isEqualTo(customBefore);
        var actual = new java.util.ArrayList<>(tuples(questions.export()));
        assertThat(actual.remove(Arrays.asList(custom))).isTrue();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(tuples(ShippedQuestions.rows()));
    }

    @Test
    void expansionPreservesUsersScoresAndActiveRoundSnapshots() {
        jdbc.sql("insert into users (username, display_name, password_hash) values ('player', 'Player', 'unused')").update();
        var rounds = new RoundService(jdbc, questions);
        int active = rounds.start("active", "player", null, null);
        rounds.serve(active, "active", "player");
        jdbc.sql("insert into rounds (user_id, score, correct_count, finished_at) select id, 20, 2, current_timestamp from users").update();
        var snapshots = jdbc.sql("select * from round_questions order by round_id, position").query().listOfRows();
        var history = jdbc.sql("select * from rounds order by id").query().listOfRows();
        var users = jdbc.sql("select * from users order by id").query().listOfRows();
        upgrade();
        assertCanonical();
        assertThat(jdbc.sql("select * from round_questions order by round_id, position").query().listOfRows()).isEqualTo(snapshots);
        assertThat(jdbc.sql("select * from rounds order by id").query().listOfRows()).isEqualTo(history);
        assertThat(jdbc.sql("select * from users order by id").query().listOfRows()).isEqualTo(users);
        while (!rounds.summary(active, "active", "player").finished()) {
            var page = rounds.serve(active, "active", "player");
            rounds.answer(active, page.position(), page.question().getAnswer(), "active", "player");
        }
        assertThat(rounds.summary(active, "active", "player").score()).isEqualTo(50);
    }

    @Test
    void allFifteenSelectionsHaveSevenQuestionsAndFiveDistinctSnapshots() {
        upgrade();
        var rounds = new RoundService(jdbc, questions);
        var prompts = new HashSet<String>();
        for (var row : questions.export().subList(1, 106)) {
            assertThat(prompts.add(normalize(row[0]))).isTrue();
            assertThat(Arrays.stream(row, 1, 5).map(QuestionBankMigrationTest::normalize).distinct().count()).isEqualTo(4);
            assertThat(Arrays.stream(row, 1, 5).filter(row[5]::equals).count()).isEqualTo(1);
        }
        for (String category : rounds.setup(null, null).categories()) {
            for (String difficulty : QuestionRepository.DIFFICULTIES) {
                assertThat(rounds.setup(category, difficulty).matchingCount()).isEqualTo(7);
                int id = rounds.start("selection", null, category, difficulty);
                var seen = new HashSet<String>();
                while (!rounds.summary(id, "selection", null).finished()) {
                    var page = rounds.serve(id, "selection", null);
                    assertThat(seen.add(page.question().getQuestion())).isTrue();
                    assertThat(questions.findAll()).anySatisfy(question -> {
                        assertThat(question.getQuestion()).isEqualTo(page.question().getQuestion());
                        assertThat(question.getCategory()).isEqualTo(category);
                        assertThat(question.getDifficulty()).isEqualTo(difficulty);
                    });
                    rounds.answer(id, page.position(), page.question().getAnswer(), "selection", null);
                }
                assertThat(seen).hasSize(5);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json", "xml"})
    void expandedBankRoundTripsEveryFormat(String format) throws Exception {
        upgrade();
        var before = questions.export();
        var adapter = new RowFormats().of(format);
        var bytes = new ByteArrayOutputStream();
        adapter.write(bytes, before);
        questions.replaceAll(adapter.read(new ByteArrayInputStream(bytes.toByteArray())));
        assertThat(tuples(questions.export())).isEqualTo(tuples(before));
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
