package com.example.Controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;

import com.example.Authentication.CurrentPlayer;
import com.example.Persistence.RoundRepository;
import com.example.Persistence.RoundRepository.HistoryEntry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The signed-in player's finished rounds.
 */
@Controller
public class StatsController {

    private static final DateTimeFormatter HISTORY_DATE = DateTimeFormatter.ofPattern("dd MMM uuuu, HH:mm", Locale.ENGLISH);

    private final RoundRepository rounds;
    private final CurrentPlayer user;

    public StatsController(RoundRepository rounds, CurrentPlayer user) {
        this.rounds = rounds;
        this.user = user;
    }

    @GetMapping("/stats")
    public String showStats(Model model) {
        model.addAttribute("player", user.getUserInfo());

        List<HistoryEntry> history = rounds.historyFor(user.getUsername());
        List<Integer> scores = history.stream().sorted(Comparator.comparingInt(HistoryEntry::id))
                .map(HistoryEntry::score).toList();
        model.addAttribute("scores", scores);
        model.addAttribute("best", scores.stream().mapToInt(Integer::intValue).max().orElse(0));
        model.addAttribute("history", history.stream().map(this::historyRow).toList());
        return "stats";
    }

    private Map<String, Object> historyRow(HistoryEntry entry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("score", entry.score());
        row.put("finishedAt", entry.finishedAt() == null ? null : entry.finishedAt().toString());
        row.put("finishedAtLabel", entry.finishedAt() == null ? null : HISTORY_DATE.format(entry.finishedAt()));
        row.put("correctCount", entry.correctCount());
        row.put("totalQuestions", entry.totalQuestions());
        return row;
    }
}
