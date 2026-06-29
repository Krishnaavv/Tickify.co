package com.ticketbooking.exception;

import java.util.UUID;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException(UUID eventId, UUID seatId) {
        super(String.format("Seat %s not found in event %s.", seatId, eventId));
    }
}
