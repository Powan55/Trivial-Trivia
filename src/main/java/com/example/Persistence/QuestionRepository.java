package com.example.Persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.example.Game.Question;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class QuestionRepository {
    public static final int COLUMNS = 6;
    public static final String[] LEGACY_HEADER =
            {"Question", "Option1", "Option2", "Option3", "Option4", "Answer"};
    public static final String[] HEADER =
            {"Question", "Option1", "Option2", "Option3", "Option4", "Answer", "Category", "Difficulty"};
    public static final List<String> DIFFICULTIES = List.of("EASY", "MEDIUM", "HARD");
    private final JdbcClient jdbc;

    public QuestionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Question> findAll() {
        return jdbc.sql("""
                select q.text, q.option_1, q.option_2, q.option_3, q.option_4, q.answer,
                       coalesce(c.name, 'Uncategorized') as category, q.difficulty
                from questions q left join categories c on c.id = q.category_id
                order by q.id
                """)
                .query((rs, row) -> new Question(rs.getString("text"), rs.getString("option_1"),
                        rs.getString("option_2"), rs.getString("option_3"), rs.getString("option_4"),
                        rs.getString("answer"), rs.getString("category"), rs.getString("difficulty")))
                .list();
    }

    public int count() {
        return jdbc.sql("select count(*) from questions").query(Integer.class).single();
    }

    public List<String[]> export() {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER.clone());
        for (Question question : findAll()) {
            rows.add(new String[] {question.getQuestion(), question.getOption1(), question.getOption2(),
                    question.getOption3(), question.getOption4(), question.getAnswer(),
                    question.getCategory(), question.getDifficulty()});
        }
        return rows;
    }

    @Transactional
    public void replaceAll(List<String[]> rowsWithHeader) {
        List<String[]> rows = validate(rowsWithHeader);
        jdbc.sql("delete from questions").update();
        for (String[] row : rows) {
            jdbc.sql("insert into categories (name) select ? where not exists (select 1 from categories where name = ?)")
                    .params(row[6], row[6]).update();
            jdbc.sql("""
                    insert into questions (text, option_1, option_2, option_3, option_4, answer, category_id, difficulty)
                    select ?, ?, ?, ?, ?, ?, id, ? from categories where name = ?
                    """)
                    .params(row[0], row[1], row[2], row[3], row[4], row[5], row[7], row[6]).update();
        }
    }

    private static List<String[]> validate(List<String[]> input) {
        if (input == null || input.isEmpty()
                || !(Arrays.equals(input.get(0), HEADER) || Arrays.equals(input.get(0), LEGACY_HEADER))) {
            throw new IllegalArgumentException("Use the six-column or eight-column question header.");
        }
        int width = input.get(0).length;
        List<String[]> rows = new ArrayList<>();
        for (String[] source : input.subList(1, input.size())) {
            if (source == null || source.length != width) {
                throw new IllegalArgumentException("Every row must match the header's column count.");
            }
            if (Arrays.stream(source).anyMatch(cell -> cell == null)) {
                throw new IllegalArgumentException("Question cells cannot be null.");
            }
            if (Arrays.stream(source, 0, COLUMNS).anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("Question, options and answer cannot be blank.");
            }
            if (Arrays.stream(source, 1, 5).noneMatch(source[5]::equals)) {
                throw new IllegalArgumentException("Every answer has to be one of that question's four options.");
            }
            String[] row = Arrays.copyOf(source, HEADER.length);
            row[6] = width == COLUMNS || source[6].isBlank() ? "Uncategorized" : source[6].trim();
            row[7] = width == COLUMNS || source[7].isBlank() ? "MEDIUM" : source[7].trim().toUpperCase(Locale.ROOT);
            if (row[6].length() > 60) {
                throw new IllegalArgumentException("Category names must be at most 60 characters.");
            }
            if (!DIFFICULTIES.contains(row[7])) {
                throw new IllegalArgumentException("Difficulty must be EASY, MEDIUM or HARD.");
            }
            rows.add(row);
        }
        return rows;
    }
}
