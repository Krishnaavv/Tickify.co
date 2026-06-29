package com.ticketbooking.saga;

import com.ticketbooking.booking.BookingService;
import com.ticketbooking.common.event.PaymentCapturedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingConfirmationConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingConfirmationConsumer.class);
    private final BookingService bookingService;

    public BookingConfirmationConsumer(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @KafkaListener(topics = SagaTopics.PAYMENT_CAPTURED, groupId = "booking-confirmation-group")
    public void confirmBooking(PaymentCapturedEvent event) {
        log.info("Confirming booking for reservation {} (Event: {}, Seat: {})", 
                event.getReservationId(), event.getEventId(), event.getSeatId());

        bookingService.confirmSeat(event.getEventId(), event.getSeatId());
    }
}
