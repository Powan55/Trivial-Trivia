package com.example.Persistence;

import java.util.ArrayList;
import java.util.List;

import com.example.Game.Question;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The question set.
 *
 * <p>Replaces {@code Game.Questions}, which read a CSV and skipped rows it could not parse. Rows
 * that cannot be parsed come from an import, so that check now lives on the import path, where
 * whoever uploaded the file can be told what was wrong with it.</p>
 */
@Repository
public class QuestionRepository {

    /** The question, four options and the answer. */
    public static final int COLUMNS = 6;

    /** The header row export writes and import expects. */
    public static final String[] HEADER =
            {"Question", "Option1", "Option2", "Option3", "Option4", "Answer"};

    private final JdbcClient jdbc;

    public QuestionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Question> findAll() {
        return jdbc.sql("""
                select text, option_1, option_2, option_3, option_4, answer
                from questions
                order by id
                """)
                .query((rs, row) -> new Question(rs.getString("text"), rs.getString("option_1"),
                        rs.getString("option_2"), rs.getString("option_3"),
                        rs.getString("option_4"), rs.getString("answer")))
                .list();
    }

    public int count() {
        return jdbc.sql("select count(*) from questions").query(Integer.class).single();
    }

    /** The whole set as rows, header first, ready for any of the three formats. */
    public List<String[]> export() {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER.clone());
        for (Question question : findAll()) {
            rows.add(new String[] {question.getQuestion(), question.getOption1(),
                    question.getOption2(), question.getOption3(), question.getOption4(),
                    question.getAnswer()});
        }
        return rows;
    }

    /**
     * Replaces the whole set with the rows below the header, in one transaction.
     *
     * <p>Rows are the caller's problem until here: an import that fails halfway leaves the old
     * set in place rather than half of each.</p>
     */
    @Transactional
    public void replaceAll(List<String[]> rowsWithHeader) {
        jdbc.sql("delete from questions").update();
        for (String[] row : rowsWithHeader.subList(1, rowsWithHeader.size())) {
            jdbc.sql("""
                    insert into questions (text, option_1, option_2, option_3, option_4, answer)
                    values (?, ?, ?, ?, ?, ?)
                    """)
                    .params(row[0], row[1], row[2], row[3], row[4], row[5])
                    .update();
        }
    }
}
