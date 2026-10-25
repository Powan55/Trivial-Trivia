package com.example.api.v1.dto;

import com.example.Persistence.RoundRepository.HistoryEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record RoundHistoryDto(int score,
        @Nullable String finishedAt, @Nullable Integer correctCount, @Nullable Integer totalQuestions) {

    public static RoundHistoryDto from(HistoryEntry entry) {
        return new RoundHistoryDto(entry.score(), entry.finishedAt() == null ? null : entry.finishedAt().toString(),
                entry.correctCount(), entry.totalQuestions());
    }
}
