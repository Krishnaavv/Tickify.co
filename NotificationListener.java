package com.ticketbooking.notification.listener;

import com.ticketbooking.common.event.PaymentCapturedEvent;
import com.ticketbooking.common.event.TicketGenerationFailedEvent;
import com.ticketbooking.saga.SagaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${simulation.fail-ticket:false}")
    private boolean failTicket;

    public NotificationListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = SagaTopics.PAYMENT_CAPTURED, groupId = "notification-service-group")
    public void generateTicketsAndNotify(PaymentCapturedEvent event) {
        log.info("Generating tickets for reservation {} (Customer: {})", event.getReservationId(), event.getCustomerId());

        // Simulate PDF rendering / barcode generation delay
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulating failure condition
        boolean shouldFail = failTicket || (event.getCustomerId() != null && event.getCustomerId().startsWith("fail_ticket"));
        if (shouldFail) {
            log.error("Ticket generation failed for customer: {}", event.getCustomerId());
            TicketGenerationFailedEvent failedEvent = TicketGenerationFailedEvent.builder()
                    .reservationId(event.getReservationId())
                    .eventId(event.getEventId())
                    .seatId(event.getSeatId())
                    .customerId(event.getCustomerId())
                    .reason("PDF compilation engine crashed simulation")
                    .build();
            kafkaTemplate.send(SagaTopics.TICKET_GENERATION_FAILED, event.getEventId().toString(), failedEvent);
        } else {
            // Success flow
            log.info("-----------------------------------------------------------------");
            log.info("TICKET ISSUED SUCCESSFULLY!");
            log.info("Reservation ID: {}", event.getReservationId());
            log.info("Event ID: {}", event.getEventId());
            log.info("Seat ID: {}", event.getSeatId());
            log.info("Customer ID: {}", event.getCustomerId());
            log.info("Barcode: https://cdn.ticketbooking.com/barcodes/tkt-" + UUID.randomUUID() + ".png");
            log.info("-----------------------------------------------------------------");
        }
    }
}
