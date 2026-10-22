package com.example.Persistence;

import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.*;

class CategoryMigrationTest {
    @Test
    void upgradeTagsOnlyExactShippedRowsWithLegacyMetadata() {
        String url = "jdbc:h2:mem:metadata_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        var source = new DriverManagerDataSource(url, "sa", "");
        Flyway.configure().dataSource(source).target("3").load().migrate();
        var jdbc = JdbcClient.create(source);
        jdbc.sql("update questions set option_1 = 'Custom Venus' where id = 2").update();
        jdbc.sql("insert into categories (name) values ('Custom category')").update();
        jdbc.sql("update questions set category_id = (select id from categories where name = 'Custom category') where id = 3").update();
        jdbc.sql("update questions set difficulty = 'HARD' where id = 4").update();
        jdbc.sql("insert into rounds (score, correct_count, finished_at) values (100, 10, current_timestamp)").update();
        Flyway.configure().dataSource(source).load().migrate();
        var questions = new QuestionRepository(jdbc).findAll();
        assertThat(questions).hasSize(10);
        assertThat(questions.get(0).getCategory()).isEqualTo("Geography");
        assertThat(questions.get(0).getDifficulty()).isEqualTo("EASY");
        assertThat(questions.get(1).getCategory()).isEqualTo("Uncategorized");
        assertThat(questions.get(1).getOption1()).isEqualTo("Custom Venus");
        assertThat(questions.get(2).getCategory()).isEqualTo("Custom category");
        assertThat(questions.get(2).getDifficulty()).isEqualTo("MEDIUM");
        assertThat(questions.get(3).getDifficulty()).isEqualTo("HARD");
        assertThat(questions.get(3).getCategory()).isEqualTo("Uncategorized");
        assertThat(jdbc.sql("select score from rounds").query(Integer.class).single()).isEqualTo(100);
        assertThat(jdbc.sql("select count(*) from rounds where selected_category is null and selected_difficulty is null")
                .query(Integer.class).single()).isEqualTo(1);
    }
}
