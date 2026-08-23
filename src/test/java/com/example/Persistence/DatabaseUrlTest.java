package com.example.Persistence;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class DatabaseUrlTest {

    private final DatabaseUrl processor = new DatabaseUrl();

    @Test
    void turnsAHostsPostgresUriIntoAJdbcUrlAndCredentials() {
        assertThat(DatabaseUrl.translate("postgres://ada:s3cret@db.example.com:5432/trivia"))
                .containsExactly(
                        entry("spring.datasource.url", "jdbc:postgresql://db.example.com:5432/trivia"),
                        entry("spring.datasource.username", "ada"),
                        entry("spring.datasource.password", "s3cret"));
    }

    @Test
    void acceptsThePostgresqlSpellingToo() {
        assertThat(DatabaseUrl.translate("postgresql://ada:s3cret@db.example.com/trivia"))
                .containsEntry("spring.datasource.url", "jdbc:postgresql://db.example.com/trivia");
    }

    @Test
    void keepsTheQueryStringBecauseThatIsWhereSslmodeLives() {
        assertThat(DatabaseUrl.translate("postgres://ada:s3cret@db.example.com/trivia?sslmode=require"))
                .containsEntry("spring.datasource.url",
                        "jdbc:postgresql://db.example.com/trivia?sslmode=require");
    }

    @Test
    void decodesAnEscapedPassword() {
        assertThat(DatabaseUrl.translate("postgres://ada:p%40ss%20word@db.example.com/trivia"))
                .containsEntry("spring.datasource.password", "p@ss word");
    }

    @Test
    void aUriWithNoPasswordStillYieldsAUsername() {
        assertThat(DatabaseUrl.translate("postgres://ada@db.example.com/trivia"))
                .containsEntry("spring.datasource.username", "ada")
                .doesNotContainKey("spring.datasource.password");
    }

    @Test
    void leavesAnythingThatIsNotAPostgresUriAlone() {
        assertThat(DatabaseUrl.translate(null)).isEmpty();
        assertThat(DatabaseUrl.translate("   ")).isEmpty();
        assertThat(DatabaseUrl.translate("not a url at all")).isEmpty();
        assertThat(DatabaseUrl.translate("mysql://ada:s3cret@db.example.com/trivia")).isEmpty();
        assertThat(DatabaseUrl.translate("jdbc:postgresql://db.example.com/trivia")).isEmpty();
    }

    @Test
    void contributesThePropertiesSpringActuallyBinds() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://ada:s3cret@db.example.com:5432/trivia");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com:5432/trivia");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("ada");
    }

    /** Set SPRING_DATASOURCE_URL yourself and that is what runs, whatever DATABASE_URL says. */
    @Test
    void anExplicitDatasourceUrlOutranksTheTranslation() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://ada:s3cret@db.example.com/trivia")
                .withProperty("spring.datasource.url", "jdbc:postgresql://chosen.example.com/trivia");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://chosen.example.com/trivia");
    }

    @Test
    void addsNothingWhenThereIsNoDatabaseUrl() {
        MockEnvironment environment = new MockEnvironment();

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getPropertySources().contains(DatabaseUrl.SOURCE_NAME)).isFalse();
    }

    /** Production with no database fails here, where the message can name what to set. */
    @Test
    void productionWithNoDatabaseAtAllRefusesToStart() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        assertThatThrownBy(() -> processor.postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SPRING_DATASOURCE_URL")
                .hasMessageContaining("DATABASE_URL");
    }

    @Test
    void productionIsHappyWithEitherVariable() {
        MockEnvironment fromSpring = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://db.example.com/trivia");
        fromSpring.setActiveProfiles("prod");
        processor.postProcessEnvironment(fromSpring, null);

        MockEnvironment fromHost = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://ada:s3cret@db.example.com/trivia");
        fromHost.setActiveProfiles("prod");
        processor.postProcessEnvironment(fromHost, null);

        assertThat(fromHost.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com/trivia");
    }

    /** Development has its own H2 file and must not be caught by the production check. */
    @Test
    void developmentWithNoDatabaseUrlIsLeftAlone() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getPropertySources().contains(DatabaseUrl.SOURCE_NAME)).isFalse();
    }
}
