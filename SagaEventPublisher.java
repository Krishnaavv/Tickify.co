package com.ticketbooking.saga;

import com.ticketbooking.common.event.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SagaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SagaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReservationReserved(ReservationReservedEvent event) {
        kafkaTemplate.send(SagaTopics.RESERVATION_RESERVED, event.getEventId().toString(), event);
    }

    public void publishPaymentCaptured(PaymentCapturedEvent event) {
        kafkaTemplate.send(SagaTopics.PAYMENT_CAPTURED, event.getEventId().toString(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(SagaTopics.PAYMENT_FAILED, event.getEventId().toString(), event);
    }

    public void publishTicketGenerationFailed(TicketGenerationFailedEvent event) {
        kafkaTemplate.send(SagaTopics.TICKET_GENERATION_FAILED, event.getEventId().toString(), event);
    }

    public void publishSeatReleaseRequested(SeatReleaseRequestedEvent event) {
        kafkaTemplate.send(SagaTopics.SEAT_RELEASE_REQUESTED, event.getEventId().toString(), event);
    }

    public void publishPaymentReversalRequested(PaymentReversalRequestedEvent event) {
        // Here, we can key by reservationId
        kafkaTemplate.send(SagaTopics.PAYMENT_REVERSAL_REQUESTED, event.getReservationId().toString(), event);
    }
}
