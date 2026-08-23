package com.example.Persistence;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Finished rounds, which is what the scores page reads.
 *
 * <p>Recording a round used to mean reading every stored score, appending one and writing the
 * whole file back, so two rounds finishing together lost one of them. It is now one insert.</p>
 */
@Repository
public class RoundRepository {

    private final JdbcClient jdbc;

    public RoundRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Stores a finished round against a username.
     *
     * @return false if no such user, in which case nothing is stored. A guest's round has no
     *         username to key it to and never reaches here.
     */
    public boolean record(String username, int score, int correct, int wrong) {
        if (username == null) {
            return false;
        }
        return jdbc.sql("""
                insert into rounds (user_id, mode, score, correct_count, wrong_count, finished_at)
                select u.id, 'SOLO', ?, ?, ?, current_timestamp
                from users u
                where u.username = ?
                """)
                .params(score, correct, wrong, username)
                .update() == 1;
    }

    /** Every finished round's score for a username, oldest first. */
    public List<Integer> scoresFor(String username) {
        if (username == null) {
            return List.of();
        }
        return jdbc.sql("""
                select r.score
                from rounds r
                join users u on u.id = r.user_id
                where u.username = ? and r.finished_at is not null
                order by r.id
                """)
                .param(username)
                .query(Integer.class)
                .list();
    }
}
