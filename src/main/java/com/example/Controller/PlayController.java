package com.example.Controller;

import com.example.Authentication.User;
import com.example.Database.Database;
import com.example.Game.Question;
import com.example.Service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Drives a round: start, one question at a time, then the final score.
 */
@Controller
@RequestMapping("/play")
public class PlayController {

    private final GameService gameService;
    private final Database database;
    private final User user;

    public PlayController(GameService gameService, Database database, User user) {
        this.gameService = gameService;
        this.database = database;
        this.user = user;
    }

    @GetMapping
    public String startGame() {
        gameService.startGame();
        return "redirect:/play/question";
    }

    @GetMapping("/question")
    public String getQuestion(Model model) {
        Question question = gameService.getNextQuestion();
        if (question == null) {
            return "redirect:/play/end";
        }
        model.addAttribute("question", question);
        return "questionPage";
    }

    @PostMapping("/answer")
    public String submitAnswer(@RequestParam String answer) {
        gameService.submitAnswer(gameService.getCurrentQuestion(), answer);
        return "redirect:/play/question";
    }

    @GetMapping("/end")
    public String endGame(Model model) {
        if (gameService.claimUnrecordedRound()) {
            user.saveScore(database, gameService.getScore());
        }
        model.addAttribute("score", gameService.getScore());
        model.addAttribute("right", gameService.getRight());
        model.addAttribute("wrong", gameService.getWrong());
        model.addAttribute("authenticated", user.isAuthenticated());
        return "endGamePage";
    }
}
