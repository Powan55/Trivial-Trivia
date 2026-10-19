package com.example.Controller;

import java.util.Map;
import java.util.Objects;

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
    private static final String ACTIVE_ROUND = "activeRoundId";
    private static final String ROUND_OWNER = "roundOwner";
    private final RoundService rounds;
    private final CurrentPlayer user;

    public PlayController(RoundService rounds, CurrentPlayer user) {
        this.rounds = rounds;
        this.user = user;
    }

    @GetMapping
    public String startGame(HttpServletRequest request) {
        var session = request.getSession();
        String owner = java.util.UUID.randomUUID().toString();
        int id = rounds.start(owner, user.getUsername());
        session.setAttribute(ROUND_OWNER, owner);
        session.setAttribute(ACTIVE_ROUND, id);
        return "redirect:/play/question";
    }

    @GetMapping("/question")
    public String getQuestion(Model model, HttpServletRequest request, RedirectAttributes feedback) {
        Integer id = active(request);
        if (id == null) {
            return "redirect:/play";
        }
        RoundService.Page page = rounds.serve(id, (String) request.getSession().getAttribute(ROUND_OWNER), user.getUsername());
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
        if (!Objects.equals(active(request), roundId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Round is no longer active");
        }
        var verdict = rounds.answer(roundId, position, answer, (String) request.getSession().getAttribute(ROUND_OWNER), user.getUsername());
        feedback.addFlashAttribute("answerCorrect", verdict.correct());
        if (!verdict.correct()) {
            feedback.addFlashAttribute("correctAnswer", verdict.correctAnswer());
        }
        return "redirect:/play/question";
    }

    @GetMapping("/end")
    public String endGame(Model model, HttpServletRequest request) {
        Integer id = active(request);
        if (id == null) {
            return "redirect:/play";
        }
        var summary = rounds.summary(id, (String) request.getSession().getAttribute(ROUND_OWNER), user.getUsername());
        if (!summary.finished()) {
            return "redirect:/play/question";
        }
        model.addAttribute("score", summary.score());
        model.addAttribute("right", summary.right());
        model.addAttribute("wrong", summary.wrong());
        model.addAttribute("authenticated", user.isAuthenticated());
        return "endGamePage";
    }

    private Integer active(HttpServletRequest request) {
        var session = request.getSession(false);
        return session == null ? null : (Integer) session.getAttribute(ACTIVE_ROUND);
    }
}
