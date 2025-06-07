package com.bookmymovie.service;

import com.bookmymovie.dto.request.PaymentRequestDto;
import com.bookmymovie.dto.response.PaymentResponseDto;
import com.bookmymovie.entity.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class MockPaymentService {

    private static final Random random = new Random();

    // Mock payment gateway success rates
    private static final double CREDIT_CARD_SUCCESS_RATE = 0.95;
    private static final double UPI_SUCCESS_RATE = 0.90;
    private static final double NET_BANKING_SUCCESS_RATE = 0.85;
    private static final double WALLET_SUCCESS_RATE = 0.92;

    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.info("Processing mock payment for booking: {} using method: {}",
                request.getBookingReference(), request.getPaymentMethod());

        // Simulate payment processing delay
        simulateProcessingDelay();

        // Generate payment reference
        String paymentReference = generatePaymentReference(request.getPaymentMethod());

        // Determine payment outcome based on method
        boolean paymentSuccess = determinePaymentOutcome(request.getPaymentMethod());

        if (paymentSuccess) {
            return createSuccessfulPaymentResponse(request, paymentReference);
        } else {
            return createFailedPaymentResponse(request, paymentReference);
        }
    }

    public PaymentResponseDto processUpiPayment(PaymentRequestDto request) {
        log.info("Processing UPI payment for booking: {}", request.getBookingReference());

        simulateProcessingDelay();
        String paymentReference = "UPI" + System.currentTimeMillis();
        String qrCode = generateUpiQrCode(request);

        // UPI payments might need user confirmation
        boolean paymentSuccess = random.nextDouble() < UPI_SUCCESS_RATE;

        PaymentResponseDto.PaymentResponseDtoBuilder responseBuilder = PaymentResponseDto.builder()
                .bookingReference(request.getBookingReference())
                .paymentReference(paymentReference)
                .paidAmount(request.getPaymentAmount())
                .paymentMethod("UPI")
                .paymentDate(LocalDateTime.now())
                .qrCodeUrl(qrCode);

        if (paymentSuccess) {
            return responseBuilder
                    .success(true)
                    .message("UPI payment successful")
                    .paymentStatus(Booking.PaymentStatus.COMPLETED)
                    .gatewayResponse("SUCCESS: Payment completed via UPI")
                    .build();
        } else {
            return responseBuilder
                    .success(false)
                    .message("UPI payment failed. Please try again.")
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: UPI transaction declined")
                    .build();
        }
    }

    public PaymentResponseDto processCardPayment(PaymentRequestDto request) {
        log.info("Processing card payment for booking: {}", request.getBookingReference());

        simulateProcessingDelay();
        String paymentReference = "CARD" + System.currentTimeMillis();

        // Validate card details (mock validation)
        if (!isValidCardDetails(request)) {
            return PaymentResponseDto.builder()
                    .success(false)
                    .message("Invalid card details")
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: Invalid card details")
                    .build();
        }

        boolean paymentSuccess = random.nextDouble() < CREDIT_CARD_SUCCESS_RATE;

        if (paymentSuccess) {
            return PaymentResponseDto.builder()
                    .success(true)
                    .message("Card payment successful")
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paidAmount(request.getPaymentAmount())
                    .paymentStatus(Booking.PaymentStatus.COMPLETED)
                    .paymentMethod(request.getPaymentMethod().toString())
                    .paymentDate(LocalDateTime.now())
                    .gatewayResponse("SUCCESS: Card payment authorized and captured")
                    .build();
        } else {
            String failureReason = getRandomCardFailureReason();
            return PaymentResponseDto.builder()
                    .success(false)
                    .message("Card payment failed: " + failureReason)
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: " + failureReason)
                    .build();
        }
    }

    public PaymentResponseDto processNetBankingPayment(PaymentRequestDto request) {
        log.info("Processing net banking payment for booking: {}", request.getBookingReference());

        simulateProcessingDelay();
        String paymentReference = "NB" + System.currentTimeMillis();
        String redirectUrl = generateNetBankingRedirectUrl(request);

        boolean paymentSuccess = random.nextDouble() < NET_BANKING_SUCCESS_RATE;

        if (paymentSuccess) {
            return PaymentResponseDto.builder()
                    .success(true)
                    .message("Net banking payment successful")
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paidAmount(request.getPaymentAmount())
                    .paymentStatus(Booking.PaymentStatus.COMPLETED)
                    .paymentMethod("NET_BANKING")
                    .paymentDate(LocalDateTime.now())
                    .redirectUrl(redirectUrl)
                    .gatewayResponse("SUCCESS: Net banking payment completed")
                    .build();
        } else {
            return PaymentResponseDto.builder()
                    .success(false)
                    .message("Net banking payment failed")
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: Net banking transaction failed")
                    .build();
        }
    }

    public PaymentResponseDto processWalletPayment(PaymentRequestDto request) {
        log.info("Processing wallet payment for booking: {}", request.getBookingReference());

        simulateProcessingDelay();
        String paymentReference = "WALLET" + System.currentTimeMillis();

        // Check wallet balance (mock check)
        if (!hasSufficientWalletBalance(request)) {
            return PaymentResponseDto.builder()
                    .success(false)
                    .message("Insufficient wallet balance")
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: Insufficient wallet balance")
                    .build();
        }

        boolean paymentSuccess = random.nextDouble() < WALLET_SUCCESS_RATE;

        if (paymentSuccess) {
            return PaymentResponseDto.builder()
                    .success(true)
                    .message("Wallet payment successful")
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paidAmount(request.getPaymentAmount())
                    .paymentStatus(Booking.PaymentStatus.COMPLETED)
                    .paymentMethod("WALLET")
                    .paymentDate(LocalDateTime.now())
                    .gatewayResponse("SUCCESS: Wallet payment debited successfully")
                    .build();
        } else {
            return PaymentResponseDto.builder()
                    .success(false)
                    .message("Wallet payment failed")
                    .bookingReference(request.getBookingReference())
                    .paymentReference(paymentReference)
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: Wallet payment processing error")
                    .build();
        }
    }

    public PaymentResponseDto refundPayment(String paymentReference, BigDecimal refundAmount, String reason) {
        log.info("Processing refund for payment: {} amount: {}", paymentReference, refundAmount);

        simulateProcessingDelay();
        String refundReference = "REF" + System.currentTimeMillis();

        // Mock refund processing - 95% success rate
        boolean refundSuccess = random.nextDouble() < 0.95;

        if (refundSuccess) {
            return PaymentResponseDto.builder()
                    .success(true)
                    .message("Refund processed successfully")
                    .paymentReference(refundReference)
                    .paidAmount(refundAmount)
                    .paymentStatus(Booking.PaymentStatus.REFUNDED)
                    .paymentDate(LocalDateTime.now())
                    .gatewayResponse("SUCCESS: Refund initiated. Amount will be credited in 3-5 business days")
                    .build();
        } else {
            return PaymentResponseDto.builder()
                    .success(false)
                    .message("Refund processing failed")
                    .paymentReference(refundReference)
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: Refund could not be processed at this time")
                    .build();
        }
    }

    public boolean verifyPayment(String paymentReference) {
        log.info("Verifying payment: {}", paymentReference);

        // Mock verification - check if payment reference exists and is valid
        return paymentReference != null &&
                paymentReference.length() > 10 &&
                (paymentReference.startsWith("CARD") ||
                        paymentReference.startsWith("UPI") ||
                        paymentReference.startsWith("NB") ||
                        paymentReference.startsWith("WALLET"));
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void simulateProcessingDelay() {
        try {
            // Simulate payment gateway response time (500ms to 3 seconds)
            Thread.sleep(500 + random.nextInt(2500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String generatePaymentReference(Booking.PaymentMethod method) {
        String prefix = switch (method) {
            case CREDIT_CARD, DEBIT_CARD -> "CARD";
            case UPI -> "UPI";
            case NET_BANKING -> "NB";
            case WALLET -> "WALLET";
            case CASH -> "CASH";
            case GIFT_CARD -> "GIFT";
        };
        return prefix + System.currentTimeMillis() + random.nextInt(1000);
    }

    private boolean determinePaymentOutcome(Booking.PaymentMethod method) {
        double successRate = switch (method) {
            case CREDIT_CARD, DEBIT_CARD -> CREDIT_CARD_SUCCESS_RATE;
            case UPI -> UPI_SUCCESS_RATE;
            case NET_BANKING -> NET_BANKING_SUCCESS_RATE;
            case WALLET -> WALLET_SUCCESS_RATE;
            case CASH -> 1.0; // Cash payments always succeed
            case GIFT_CARD -> 0.98; // High success rate for gift cards
        };
        return random.nextDouble() < successRate;
    }

    private PaymentResponseDto createSuccessfulPaymentResponse(PaymentRequestDto request, String paymentReference) {
        return PaymentResponseDto.builder()
                .success(true)
                .message("Payment completed successfully")
                .bookingReference(request.getBookingReference())
                .paymentReference(paymentReference)
                .paidAmount(request.getPaymentAmount())
                .paymentStatus(Booking.PaymentStatus.COMPLETED)
                .paymentMethod(request.getPaymentMethod().toString())
                .paymentDate(LocalDateTime.now())
                .gatewayResponse("SUCCESS: Payment processed successfully")
                .build();
    }

    private PaymentResponseDto createFailedPaymentResponse(PaymentRequestDto request, String paymentReference) {
        String failureReason = getRandomFailureReason();
        return PaymentResponseDto.builder()
                .success(false)
                .message("Payment failed: " + failureReason)
                .bookingReference(request.getBookingReference())
                .paymentReference(paymentReference)
                .paymentStatus(Booking.PaymentStatus.FAILED)
                .paymentMethod(request.getPaymentMethod().toString())
                .gatewayResponse("FAILED: " + failureReason)
                .build();
    }

    private boolean isValidCardDetails(PaymentRequestDto request) {
        // Mock card validation
        if (request.getCardNumber() == null || request.getCardNumber().length() < 16) {
            return false;
        }
        if (request.getCvv() == null || request.getCvv().length() != 3) {
            return false;
        }
        if (request.getExpiryDate() == null || !request.getExpiryDate().matches("\\d{2}/\\d{2}")) {
            return false;
        }
        return request.getCardHolderName() != null && !request.getCardHolderName().trim().isEmpty();
    }

    private boolean hasSufficientWalletBalance(PaymentRequestDto request) {
        // Mock wallet balance check - assume 90% of users have sufficient balance
        return random.nextDouble() < 0.90;
    }

    private String getRandomCardFailureReason() {
        String[] reasons = {
                "Insufficient funds",
                "Card expired",
                "Invalid CVV",
                "Card blocked",
                "Transaction declined by bank",
                "Daily transaction limit exceeded",
                "Card not activated for online transactions"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String getRandomFailureReason() {
        String[] reasons = {
                "Network timeout",
                "Gateway temporarily unavailable",
                "Invalid transaction amount",
                "Payment method not supported",
                "User cancelled transaction",
                "Session expired",
                "Technical error"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    private String generateUpiQrCode(PaymentRequestDto request) {
        // Mock UPI QR code generation
        return String.format("upi://pay?pa=bookmymovie@upi&pn=BookMyMovie&am=%s&tn=Booking-%s&cu=INR",
                request.getPaymentAmount(), request.getBookingReference());
    }

    private String generateNetBankingRedirectUrl(PaymentRequestDto request) {
        // Mock net banking redirect URL
        return String.format("https://mock-bank.com/payment?amount=%s&ref=%s&merchant=bookmymovie",
                request.getPaymentAmount(), request.getBookingReference());
    }

    // ==================== PAYMENT STATUS CHECKING ====================

    public Booking.PaymentStatus checkPaymentStatus(String paymentReference) {
        log.info("Checking payment status for reference: {}", paymentReference);

        // Mock status check based on payment reference pattern
        if (paymentReference.contains("FAILED")) {
            return Booking.PaymentStatus.FAILED;
        } else if (paymentReference.contains("PENDING")) {
            return Booking.PaymentStatus.PENDING;
        } else if (paymentReference.contains("PROCESSING")) {
            return Booking.PaymentStatus.PROCESSING;
        } else {
            // Assume most payments are completed
            return Booking.PaymentStatus.COMPLETED;
        }
    }

    public PaymentResponseDto processPaymentByMethod(PaymentRequestDto request) {
        return switch (request.getPaymentMethod()) {
            case CREDIT_CARD, DEBIT_CARD -> processCardPayment(request);
            case UPI -> processUpiPayment(request);
            case NET_BANKING -> processNetBankingPayment(request);
            case WALLET -> processWalletPayment(request);
            case CASH -> processCashPayment(request);
            case GIFT_CARD -> processGiftCardPayment(request);
        };
    }

    private PaymentResponseDto processCashPayment(PaymentRequestDto request) {
        // Cash payments are always successful (handled at counter)
        return PaymentResponseDto.builder()
                .success(true)
                .message("Cash payment recorded")
                .bookingReference(request.getBookingReference())
                .paymentReference("CASH" + System.currentTimeMillis())
                .paidAmount(request.getPaymentAmount())
                .paymentStatus(Booking.PaymentStatus.COMPLETED)
                .paymentMethod("CASH")
                .paymentDate(LocalDateTime.now())
                .gatewayResponse("SUCCESS: Cash payment recorded at counter")
                .build();
    }

    private PaymentResponseDto processGiftCardPayment(PaymentRequestDto request) {
        // Mock gift card validation
        boolean validGiftCard = random.nextDouble() < 0.98;

        if (validGiftCard) {
            return PaymentResponseDto.builder()
                    .success(true)
                    .message("Gift card payment successful")
                    .bookingReference(request.getBookingReference())
                    .paymentReference("GIFT" + System.currentTimeMillis())
                    .paidAmount(request.getPaymentAmount())
                    .paymentStatus(Booking.PaymentStatus.COMPLETED)
                    .paymentMethod("GIFT_CARD")
                    .paymentDate(LocalDateTime.now())
                    .gatewayResponse("SUCCESS: Gift card redeemed successfully")
                    .build();
        } else {
            return PaymentResponseDto.builder()
                    .success(false)
                    .message("Invalid or insufficient gift card balance")
                    .bookingReference(request.getBookingReference())
                    .paymentReference("GIFT" + System.currentTimeMillis())
                    .paymentStatus(Booking.PaymentStatus.FAILED)
                    .gatewayResponse("FAILED: Gift card invalid or insufficient balance")
                    .build();
        }
    }
}