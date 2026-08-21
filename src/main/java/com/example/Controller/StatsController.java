package com.example.Controller;

import java.util.ArrayList;
import java.util.List;

import com.example.Authentication.User;
import com.example.Database.DataFiles;
import com.example.Database.Database;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The signed-in player's finished rounds.
 */
@Controller
public class StatsController {

    private static final int USERNAME = 0;
    private static final int SCORE = 1;
    private static final int COLUMNS = 2;

    private final Database database;
    private final User user;

    public StatsController(Database database, User user) {
        this.database = database;
        this.user = user;
    }

    @GetMapping("/stats")
    public String showStats(Model model) {
        model.addAttribute("player", user.getUserInfo());
        model.addAttribute("authenticated", user.isAuthenticated());

        List<Integer> scores = scoresFor(user.getUsername());
        model.addAttribute("scores", scores);
        model.addAttribute("best", scores.stream().mapToInt(Integer::intValue).max().orElse(0));
        return "stats";
    }

    private List<Integer> scoresFor(String username) {
        List<Integer> scores = new ArrayList<>();
        if (username == null) {
            return scores;
        }
        for (String[] row : database.readFile(DataFiles.STATS)) {
            if (row.length < COLUMNS || !username.equals(row[USERNAME])) {
                continue;
            }
            try {
                scores.add(Integer.parseInt(row[SCORE].trim()));
            } catch (NumberFormatException ignored) {
                // A row somebody hand-edited. Skip it rather than fail the page.
            }
        }
        return scores;
    }
}
