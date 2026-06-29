package com.ticketbooking.exception;

import java.util.UUID;

public class SeatAlreadyAllocatedException extends RuntimeException {
    public SeatAlreadyAllocatedException(UUID eventId, UUID seatId) {
        super(String.format("Seat %s in event %s is already allocated.", seatId, eventId));
    }
}
