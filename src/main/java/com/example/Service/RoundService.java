package com.example.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.example.Persistence.QuestionRepository;
import java.util.Objects;

import com.example.Game.Question;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoundService {
    private final JdbcClient jdbc;
    private final QuestionRepository questions;

    public RoundService(JdbcClient jdbc, QuestionRepository questions) {
        this.jdbc = jdbc;
        this.questions = questions;
    }

    public Setup setup(String category, String difficulty) {
        return selection(questions.findAll(), category, difficulty);
    }

    private Setup selection(List<Question> pool, String category, String difficulty) {
        category = category == null || category.isBlank() ? null : category.trim();
        difficulty = difficulty == null || difficulty.isBlank() ? null : difficulty.trim();
        List<String> categories = pool.stream().map(Question::getCategory).distinct().sorted().toList();
        if (category != null && !categories.contains(category)) {
            throw new IllegalArgumentException("Choose a category from the list.");
        }
        if (difficulty != null && !QuestionRepository.DIFFICULTIES.contains(difficulty)) {
            throw new IllegalArgumentException("Choose EASY, MEDIUM or HARD.");
        }
        String selectedCategory = category;
        String selectedDifficulty = difficulty;
        int count = (int) pool.stream().filter(question -> matches(question, selectedCategory, selectedDifficulty)).count();
        return new Setup(categories, category, difficulty, count, Math.min(5, count));
    }

    private boolean matches(Question question, String category, String difficulty) {
        return (category == null || category.equals(question.getCategory()))
                && (difficulty == null || difficulty.equals(question.getDifficulty()));
    }

    @Transactional
    public int start(String ownerToken, String username, String category, String difficulty) {
        List<Question> pool = questions.findAll();
        Setup selection = selection(pool, category, difficulty);
        List<Question> chosen = new ArrayList<>(pool.stream()
                .filter(question -> matches(question, selection.category(), selection.difficulty())).toList());
        if (chosen.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No questions match this selection.");
        }
        Collections.shuffle(chosen);
        chosen = chosen.subList(0, selection.roundSize());
        Integer userId = username == null ? null : jdbc.sql("select id from users where username = ?")
                .param(username).query(Integer.class).single();
        var key = new GeneratedKeyHolder();
        jdbc.sql("""
                insert into rounds (user_id, owner_token, selected_category, selected_difficulty, total_questions)
                values (:userId, :ownerToken, :category, :difficulty, :total)
                """).param("userId", userId, java.sql.Types.INTEGER).param("ownerToken", ownerToken)
                .param("category", selection.category(), java.sql.Types.VARCHAR)
                .param("difficulty", selection.difficulty(), java.sql.Types.VARCHAR)
                .param("total", chosen.size()).update(key, "id");
        int id = key.getKey().intValue();
        for (int index = 0; index < chosen.size(); index++) {
            Question question = chosen.get(index);
            jdbc.sql("""
                    insert into round_questions (round_id, position, text, option_1, option_2, option_3, option_4, answer)
                    values (?, ?, ?, ?, ?, ?, ?, ?)
                    """).params(id, index + 1, question.getQuestion(), question.getOption1(), question.getOption2(),
                            question.getOption3(), question.getOption4(), question.getAnswer()).update();
        }
        return id;
    }

    public record Setup(List<String> categories, String category, String difficulty, int matchingCount, int roundSize) { }

    @Transactional
    public Page serve(int id, String ownerToken, String username) {
        Summary round = owned(id, ownerToken, username, true);
        if (round.finished()) {
            return null;
        }
        Snapshot next = jdbc.sql("""
                select * from round_questions where round_id = ? and answered_at is null order by position limit 1
                """).param(id).query((rs, row) -> new Snapshot(rs.getInt("position"),
                        new Question(rs.getString("text"), rs.getString("option_1"), rs.getString("option_2"),
                                rs.getString("option_3"), rs.getString("option_4"), rs.getString("answer"))))
                .single();
        jdbc.sql("""
                update round_questions set served_at = coalesce(served_at, current_timestamp)
                where round_id = ? and position = ?
                """)
                .params(id, next.position()).update();
        return new Page(id, next.position(), round.total(), next.question());
    }

    @Transactional
    public Verdict answer(int id, int position, String answer, String ownerToken, String username) {
        Summary round = owned(id, ownerToken, username, true);
        AnswerRow question = jdbc.sql("""
                select answer, served_at, correct, option_1, option_2, option_3, option_4
                from round_questions where round_id = ? and position = ?
                """)
                .params(id, position).query((rs, row) -> new AnswerRow(rs.getString("answer"),
                        rs.getTimestamp("served_at") != null, (Boolean) rs.getObject("correct"), List.of(rs.getString("option_1"), rs.getString("option_2"), rs.getString("option_3"), rs.getString("option_4")))).optional()
                .orElseThrow(RoundService::invalid);
        if (!question.served()) {
            throw invalid();
        }
        if (question.correct() != null) {
            return new Verdict(question.correct(), question.answer());
        }
        if (round.finished() || answer == null || !question.options().contains(answer)) {
            throw invalid();
        }
        boolean correct = question.answer().equalsIgnoreCase(answer);
        jdbc.sql("""
                update round_questions set submitted_answer = ?, correct = ?, answered_at = current_timestamp
                where round_id = ? and position = ?
                """).params(answer, correct, id, position).update();
        jdbc.sql("""
                update rounds set score = score + ?, correct_count = correct_count + ?, wrong_count = wrong_count + ?,
                finished_at = case when correct_count + wrong_count + 1 = total_questions
                                   then current_timestamp else null end where id = ?
                """).params(correct ? 10 : 0, correct ? 1 : 0, correct ? 0 : 1, id).update();
        return new Verdict(correct, question.answer());
    }

    @Transactional(readOnly = true)
    public Summary summary(int id, String ownerToken, String username) {
        return owned(id, ownerToken, username, false);
    }

    private Summary owned(int id, String ownerToken, String username, boolean lock) {
        return jdbc.sql("""
                select r.*, (select username from users where id = r.user_id) as username from rounds r where r.id = ?
                """ + (lock ? " for update" : ""))
                .param(id).query((rs, row) -> {
                    if (ownerToken == null || !Objects.equals(ownerToken, rs.getString("owner_token"))
                            || !Objects.equals(username, rs.getString("username"))) {
                        throw invalid();
                    }
                    return new Summary(rs.getInt("score"), rs.getInt("correct_count"),
                            rs.getInt("wrong_count"), rs.getInt("total_questions"), rs.getTimestamp("finished_at") != null);
                }).optional().orElseThrow(RoundService::invalid);
    }

    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Round is no longer active");
    }

    public record Page(int roundId, int position, int total, Question question) { }
    public record Verdict(boolean correct, String correctAnswer) { }
    public record Summary(int score, int right, int wrong, int total, boolean finished) { }
    private record Snapshot(int position, Question question) { }
    private record AnswerRow(String answer, boolean served, Boolean correct, List<String> options) { }
}
