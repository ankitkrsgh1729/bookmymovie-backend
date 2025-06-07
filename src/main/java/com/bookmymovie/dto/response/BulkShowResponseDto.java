package com.bookmymovie.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkShowResponseDto {

    private Integer totalRequested;
    private Integer successfullyCreated;
    private Integer failed;
    private List<String> errors;
    private List<Long> createdShowIds;
    private List<ShowConflictDto> conflicts;

    @Data
    @Builder
    public static class ShowConflictDto {
        private LocalDate date;
        private LocalTime time;
        private String reason;
        private String suggestion;
    }
}