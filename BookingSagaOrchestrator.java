package com.ticketbooking.saga;

import com.ticketbooking.common.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(BookingSagaOrchestrator.class);
    private final SagaEventPublisher eventPublisher;

    public BookingSagaOrchestrator(SagaEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = SagaTopics.PAYMENT_FAILED, groupId = "saga-orchestrator-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Payment failed for reservation {}. Initiating compensation: seat release.", event.getReservationId());
        
        SeatReleaseRequestedEvent releaseEvent = SeatReleaseRequestedEvent.builder()
                .reservationId(event.getReservationId())
                .eventId(event.getEventId())
                .seatId(event.getSeatId())
                .customerId(event.getCustomerId())
                .reason(event.getReason())
                .build();
        
        eventPublisher.publishSeatReleaseRequested(releaseEvent);
    }

    @KafkaListener(topics = SagaTopics.TICKET_GENERATION_FAILED, groupId = "saga-orchestrator-group")
    public void handleTicketGenerationFailed(TicketGenerationFailedEvent event) {
        log.error("Ticket generation failed for reservation {}. Initiating compensation: payment refund and seat release.", 
                event.getReservationId());

        // 1. Request Payment Reversal
        PaymentReversalRequestedEvent reversalEvent = PaymentReversalRequestedEvent.builder()
                .reservationId(event.getReservationId())
                .customerId(event.getCustomerId())
                .amount(null) // Real implementation would track original amount
                .reason("Ticket generation failed: " + event.getReason())
                .build();
        eventPublisher.publishPaymentReversalRequested(reversalEvent);

        // 2. Request Seat Release
        SeatReleaseRequestedEvent releaseEvent = SeatReleaseRequestedEvent.builder()
                .reservationId(event.getReservationId())
                .eventId(event.getEventId())
                .seatId(event.getSeatId())
                .customerId(event.getCustomerId())
                .reason("Ticket generation failed: " + event.getReason())
                .build();
        eventPublisher.publishSeatReleaseRequested(releaseEvent);
    }
}
