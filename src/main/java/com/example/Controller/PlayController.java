package com.example.Controller;

import java.util.Map;

import com.example.Authentication.CurrentPlayer;
import com.example.Game.Question;
import com.example.Persistence.RoundRepository;
import com.example.Service.GameService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Drives a round: start, one question at a time, then the final score.
 */
@Controller
@RequestMapping("/play")
public class PlayController {

    private final GameService gameService;
    private final RoundRepository rounds;
    private final CurrentPlayer user;

    public PlayController(GameService gameService, RoundRepository rounds, CurrentPlayer user) {
        this.gameService = gameService;
        this.rounds = rounds;
        this.user = user;
    }

    @GetMapping
    public String startGame() {
        gameService.startGame();
        return "redirect:/play/question";
    }

    @GetMapping("/question")
    public String getQuestion(Model model, HttpServletRequest request, RedirectAttributes feedback) {
        Question question = gameService.getNextQuestion();
        if (question == null) {
            // The last answer's verdict was flashed at this hop, which is only a staging post once
            // the round is over. Pass it on so the end page can show it.
            Map<String, ?> carried = RequestContextUtils.getInputFlashMap(request);
            if (carried != null) {
                carried.forEach(feedback::addFlashAttribute);
            }
            return "redirect:/play/end";
        }
        model.addAttribute("question", question);
        model.addAttribute("questionNumber", gameService.getQuestionNumber());
        model.addAttribute("totalQuestions", gameService.getTotalQuestions());
        return "questionPage";
    }

    @PostMapping("/answer")
    public String submitAnswer(@RequestParam String answer, RedirectAttributes feedback) {
        Question asked = gameService.getCurrentQuestion();
        if (asked == null) {
            return "redirect:/play/question";
        }
        boolean correct = gameService.submitAnswer(asked, answer);
        feedback.addFlashAttribute("answerCorrect", correct);
        if (!correct) {
            feedback.addFlashAttribute("correctAnswer", asked.getAnswer());
        }
        return "redirect:/play/question";
    }

    @GetMapping("/end")
    public String endGame(Model model) {
        // A guest's round is not stored: there is nothing to key it to.
        if (gameService.claimUnrecordedRound() && user.isAuthenticated()) {
            rounds.record(user.getUsername(), gameService.getScore(),
                    gameService.getRight(), gameService.getWrong());
        }
        model.addAttribute("score", gameService.getScore());
        model.addAttribute("right", gameService.getRight());
        model.addAttribute("wrong", gameService.getWrong());
        model.addAttribute("authenticated", user.isAuthenticated());
        return "endGamePage";
    }
}
