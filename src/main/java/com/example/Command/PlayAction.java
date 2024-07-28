package com.example.Command;

import com.example.Authentication.ProxyUser;
import com.example.Game.Question;
import com.example.Service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/play")
public class PlayAction {

    private final GameService gameService;

    @Autowired
    public PlayAction(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public String startGame(HttpSession session, Model model) {
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            // Handle the case where the user is not found in the session
//            return "redirect:/loginMenu";
//        }

        // Note: This implementation is temporary.
        // We are using a guest user (ProxyUser) to start the game until the User class is fully developed.
        gameService.startGame(new ProxyUser());

        return "redirect:/play/question";
    }

    @GetMapping("/question")
    public String getQuestion(Model model, HttpSession session) {
        Question question = gameService.getNextQuestion();
        if (question == null) {
            return "redirect:/play/end";
        }
        model.addAttribute("question", question);
        return "questionPage"; // This is the name of the JSP view to render the question
    }

    @PostMapping("/answer")
    public String submitAnswer(HttpSession session, @RequestParam String answer, Model model) {
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            // Handle the case where the user is not found in the session
//            return "redirect:/login";
//        }
        Question currentQuestion = gameService.getNextQuestion(); // or another method to get the current question
        gameService.submitAnswer(new ProxyUser(), currentQuestion, answer);
        return "redirect:/play/question";
    }

    @GetMapping("/end")
    public String endGame(HttpSession session, Model model) {
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            // Handle the case where the user is not found in the session
//            return "redirect:/login";
//        }
        int finalScore = gameService.getScore(new ProxyUser());
        model.addAttribute("score", finalScore);
        gameService.endGame(new ProxyUser());
        return "endGamePage"; // This is the name of the JSP view to render the final score
    }
}
