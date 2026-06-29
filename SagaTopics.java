package com.ticketbooking.saga;

public final class SagaTopics {
    private SagaTopics() {}

    public static final String RESERVATION_RESERVED = "reservation.reserved";
    public static final String PAYMENT_CAPTURED = "payment.captured";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String TICKET_GENERATION_FAILED = "ticket.generation.failed";
    public static final String SEAT_RELEASE_REQUESTED = "seat.release.requested";
    public static final String PAYMENT_REVERSAL_REQUESTED = "payment.reversal.requested";
}
