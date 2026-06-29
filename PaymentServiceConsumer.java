package com.ticketbooking.saga;

import com.ticketbooking.common.event.PaymentCapturedEvent;
import com.ticketbooking.common.event.PaymentFailedEvent;
import com.ticketbooking.common.event.PaymentReversalRequestedEvent;
import com.ticketbooking.common.event.ReservationReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentServiceConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceConsumer.class);
    private final SagaEventPublisher eventPublisher;

    @Value("${simulation.fail-payment:false}")
    private boolean failPayment;

    public PaymentServiceConsumer(SagaEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = SagaTopics.RESERVATION_RESERVED, groupId = "payment-service-group")
    public void processPayment(ReservationReservedEvent event) {
        log.info("Processing payment for reservation {} (Customer: {}, Amount: {})", 
                event.getReservationId(), event.getCustomerId(), event.getFinalPrice());

        // Simulate gateway call delay
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulating failure condition
        boolean shouldFail = failPayment || (event.getCustomerId() != null && event.getCustomerId().startsWith("fail_payment"));
        if (shouldFail) {
            log.error("Payment failed for customer: {}", event.getCustomerId());
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .reservationId(event.getReservationId())
                    .eventId(event.getEventId())
                    .seatId(event.getSeatId())
                    .customerId(event.getCustomerId())
                    .amount(event.getFinalPrice())
                    .reason("Insufficient funds simulation")
                    .build();
            eventPublisher.publishPaymentFailed(failedEvent);
        } else {
            log.info("Payment captured successfully for reservation {}", event.getReservationId());
            PaymentCapturedEvent capturedEvent = PaymentCapturedEvent.builder()
                    .reservationId(event.getReservationId())
                    .eventId(event.getEventId())
                    .seatId(event.getSeatId())
                    .customerId(event.getCustomerId())
                    .amount(event.getFinalPrice())
                    .transactionId("tx-" + UUID.randomUUID())
                    .build();
            eventPublisher.publishPaymentCaptured(capturedEvent);
        }
    }

    @KafkaListener(topics = SagaTopics.PAYMENT_REVERSAL_REQUESTED, groupId = "payment-service-group")
    public void reversePayment(PaymentReversalRequestedEvent event) {
        log.warn("Refunding payment for reservation {} (Reason: {})", 
                event.getReservationId(), event.getReason());
        // Simulate refund gateway call
    }
}
