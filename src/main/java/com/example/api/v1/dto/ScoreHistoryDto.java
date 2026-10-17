package com.example.api.v1.dto;

import java.util.List;

public record ScoreHistoryDto(List<Integer> scores, int best) {

    public static ScoreHistoryDto from(List<Integer> scores) {
        int best = scores.stream().mapToInt(Integer::intValue).max().orElse(0);
        return new ScoreHistoryDto(scores, best);
    }
}
