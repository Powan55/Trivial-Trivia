package com.example.Controller;

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

    public PlayController(GameService gameService) {
        this.gameService = gameService;
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
        model.addAttribute("score", gameService.getScore());
        model.addAttribute("right", gameService.getRight());
        model.addAttribute("wrong", gameService.getWrong());
        return "endGamePage";
    }
}
