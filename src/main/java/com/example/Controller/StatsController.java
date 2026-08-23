package com.example.Controller;

import java.util.List;

import com.example.Authentication.CurrentPlayer;
import com.example.Persistence.RoundRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The signed-in player's finished rounds.
 */
@Controller
public class StatsController {

    private final RoundRepository rounds;
    private final CurrentPlayer user;

    public StatsController(RoundRepository rounds, CurrentPlayer user) {
        this.rounds = rounds;
        this.user = user;
    }

    @GetMapping("/stats")
    public String showStats(Model model) {
        model.addAttribute("player", user.getUserInfo());

        List<Integer> scores = rounds.scoresFor(user.getUsername());
        model.addAttribute("scores", scores);
        model.addAttribute("best", scores.stream().mapToInt(Integer::intValue).max().orElse(0));
        return "stats";
    }
}
