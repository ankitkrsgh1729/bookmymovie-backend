package com.bookmymovie.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowConflictCheckResponseDto {

    private Boolean hasConflicts;
    private String message;
    private List<ConflictingShowDto> conflictingShows;
    private LocalDateTime suggestedStartTime;
    private List<LocalDateTime> alternativeSlots;

    @Data
    @Builder
    public static class ConflictingShowDto {
        private Long showId;
        private String movieTitle;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String conflictReason;
    }
}
