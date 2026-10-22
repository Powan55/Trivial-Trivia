package com.example.Controller;

import java.util.Map;

import com.example.Authentication.CurrentPlayer;
import com.example.Service.RoundService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
@RequestMapping("/play")
public class PlayController {
    private static final String ACTIVE_ROUND = "activeRound";
    private final RoundService rounds;
    private final CurrentPlayer user;

    public PlayController(RoundService rounds, CurrentPlayer user) {
        this.rounds = rounds;
        this.user = user;
    }

    @GetMapping
    public String setup(@RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty, Model model, HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            return setupModel(rounds.setup(category, difficulty), model);
        } catch (IllegalArgumentException invalid) {
            response.setStatus(400);
            model.addAttribute("error", invalid.getMessage());
            return setupModel(rounds.setup(null, null), model);
        }
    }

    @PostMapping
    public String startGame(@RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty, HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response, Model model) {
        String owner = java.util.UUID.randomUUID().toString();
        try {
            int id = rounds.start(owner, user.getUsername(), category, difficulty);
            request.getSession().setAttribute(ACTIVE_ROUND, new ActiveRound(id, owner));
            return "redirect:/play/question";
        } catch (IllegalArgumentException invalid) {
            return setupError(category, difficulty, invalid.getMessage(), 400, model, request, response);
        } catch (ResponseStatusException empty) {
            if (empty.getStatusCode().value() != 409) {
                throw empty;
            }
            return setupError(category, difficulty, empty.getReason(), 409, model, request, response);
        }
    }

    private String setupError(String category, String difficulty, String error, int status, Model model,
            HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        setup(category, difficulty, model, request, response);
        response.setStatus(status);
        model.addAttribute("error", error);
        return "playSetup";
    }

    private String setupModel(RoundService.Setup setup, Model model) {
        model.addAttribute("categories", setup.categories());
        model.addAttribute("difficulties", com.example.Persistence.QuestionRepository.DIFFICULTIES);
        model.addAttribute("selectedCategory", setup.category() == null ? "" : setup.category());
        model.addAttribute("selectedDifficulty", setup.difficulty() == null ? "" : setup.difficulty());
        model.addAttribute("matchingCount", setup.matchingCount());
        model.addAttribute("roundSize", setup.roundSize());
        return "playSetup";
    }

    @GetMapping("/question")
    public String getQuestion(Model model, HttpServletRequest request, RedirectAttributes feedback) {
        ActiveRound active = active(request);
        if (active == null) {
            return "redirect:/play";
        }
        RoundService.Page page = rounds.serve(active.id(), active.ownerToken(), user.getUsername());
        if (page == null) {
            Map<String, ?> carried = RequestContextUtils.getInputFlashMap(request);
            if (carried != null) {
                carried.forEach(feedback::addFlashAttribute);
            }
            return "redirect:/play/end";
        }
        model.addAttribute("question", page.question());
        model.addAttribute("roundId", page.roundId());
        model.addAttribute("position", page.position());
        model.addAttribute("questionNumber", page.position());
        model.addAttribute("totalQuestions", page.total());
        return "questionPage";
    }

    @PostMapping("/answer")
    public String submitAnswer(@RequestParam int roundId, @RequestParam int position,
            @RequestParam String answer, HttpServletRequest request, RedirectAttributes feedback) {
        ActiveRound active = active(request);
        if (active == null || active.id() != roundId) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Round is no longer active");
        }
        var verdict = rounds.answer(roundId, position, answer, active.ownerToken(), user.getUsername());
        feedback.addFlashAttribute("answerCorrect", verdict.correct());
        if (!verdict.correct()) {
            feedback.addFlashAttribute("correctAnswer", verdict.correctAnswer());
        }
        return "redirect:/play/question";
    }

    @GetMapping("/end")
    public String endGame(Model model, HttpServletRequest request) {
        ActiveRound active = active(request);
        if (active == null) {
            return "redirect:/play";
        }
        var summary = rounds.summary(active.id(), active.ownerToken(), user.getUsername());
        if (!summary.finished()) {
            return "redirect:/play/question";
        }
        model.addAttribute("score", summary.score());
        model.addAttribute("right", summary.right());
        model.addAttribute("wrong", summary.wrong());
        model.addAttribute("authenticated", user.isAuthenticated());
        return "endGamePage";
    }

    private ActiveRound active(HttpServletRequest request) {
        var session = request.getSession(false);
        return session == null ? null : (ActiveRound) session.getAttribute(ACTIVE_ROUND);
    }

    public record ActiveRound(int id, String ownerToken) { }
}
