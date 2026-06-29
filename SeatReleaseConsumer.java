package com.ticketbooking.saga;

import com.ticketbooking.booking.BookingService;
import com.ticketbooking.common.event.SeatReleaseRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SeatReleaseConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeatReleaseConsumer.class);
    private final BookingService bookingService;

    public SeatReleaseConsumer(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @KafkaListener(topics = SagaTopics.SEAT_RELEASE_REQUESTED, groupId = "seat-release-group")
    public void releaseSeat(SeatReleaseRequestedEvent event) {
        log.info("Releasing seat {} for event {} (Reservation: {}, Reason: {})", 
                event.getSeatId(), event.getEventId(), event.getReservationId(), event.getReason());

        bookingService.releaseSeat(event.getEventId(), event.getSeatId(), event.getCustomerId());
    }
}
