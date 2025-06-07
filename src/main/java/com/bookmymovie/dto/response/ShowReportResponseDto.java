package com.bookmymovie.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowReportResponseDto {

    private String reportType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime generatedAt;
    private Object reportData; // Dynamic based on report type
    private String summary;
    private List<String> insights;
}