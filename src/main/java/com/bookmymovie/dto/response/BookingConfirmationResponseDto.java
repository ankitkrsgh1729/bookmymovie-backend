package com.bookmymovie.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingConfirmationResponseDto {

    private String bookingReference;
    private Boolean confirmed;
    private String message;
    private LocalDateTime confirmationTime;
    private BookingReceiptDto receipt;
    private String qrCode;
    private List<String> nextSteps;
}

