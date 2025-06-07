package com.bookmymovie.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;




@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingReportResponseDto {

    private String reportType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private LocalDateTime generatedAt;
    private String summary;
    private Object reportData; // Dynamic based on report type
    private List<String> insights;
    private List<String> recommendations;
}
