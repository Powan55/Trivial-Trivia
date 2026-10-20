package com.example.api.v1;

import com.example.Authentication.CurrentPlayer;
import com.example.Persistence.RoundRepository;
import com.example.api.v1.dto.ScoreHistoryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoundsController {

    private final RoundRepository rounds;
    private final CurrentPlayer user;

    public RoundsController(RoundRepository rounds, CurrentPlayer user) {
        this.rounds = rounds;
        this.user = user;
    }

    @GetMapping("/api/v1/rounds")
    public ScoreHistoryDto myRounds() {
        return ScoreHistoryDto.from(rounds.scoresFor(user.getUsername()));
    }
}
