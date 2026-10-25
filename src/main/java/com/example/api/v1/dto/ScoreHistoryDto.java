package com.example.api.v1.dto;

import java.util.List;
import java.util.Comparator;
import com.example.Persistence.RoundRepository.HistoryEntry;

public record ScoreHistoryDto(List<Integer> scores, int best, List<RoundHistoryDto> rounds) {

    public static ScoreHistoryDto from(List<HistoryEntry> history) {
        List<Integer> scores = history.stream().sorted(Comparator.comparingInt(HistoryEntry::id))
                .map(HistoryEntry::score).toList();
        int best = scores.stream().mapToInt(Integer::intValue).max().orElse(0);
        return new ScoreHistoryDto(scores, best, history.stream().map(RoundHistoryDto::from).toList());
    }
}
