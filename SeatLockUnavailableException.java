package com.ticketbooking.exception;

import java.util.UUID;

public class SeatLockUnavailableException extends RuntimeException {
    public SeatLockUnavailableException(UUID eventId, UUID seatId) {
        super(String.format("Lock for seat %s in event %s is currently unavailable.", seatId, eventId));
    }
}
