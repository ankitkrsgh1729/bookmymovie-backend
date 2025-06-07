package com.bookmymovie.dto.response;

import lombok.*;
import java.time.LocalDateTime;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingNotificationDto {

    private String bookingReference;
    private String notificationType; // CONFIRMATION, REMINDER, CANCELLATION, etc.
    private String message;
    private LocalDateTime scheduledTime;
    private String channel; // EMAIL, SMS, PUSH
    private Object additionalData;
}