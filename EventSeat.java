package com.ticketbooking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSeat implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private UUID seatId;
    private String seatName;
    private SeatStatus status;
    private BigDecimal basePrice;
    private String reservedBy;
    private Instant reservedAt;
    private BigDecimal finalPrice;

    public void reserve(String customerId, Instant now, BigDecimal finalPrice) {
        if (this.status != SeatStatus.OPEN) {
            throw new IllegalStateException("Seat " + seatId + " is not OPEN. Current status: " + this.status);
        }
        this.status = SeatStatus.RESERVED;
        this.reservedBy = customerId;
        this.reservedAt = now;
        this.finalPrice = finalPrice;
    }

    public void confirm() {
        if (this.status != SeatStatus.RESERVED) {
            throw new IllegalStateException("Seat " + seatId + " cannot be confirmed. Current status: " + this.status);
        }
        this.status = SeatStatus.CONFIRMED;
    }

    public void release() {
        this.status = SeatStatus.OPEN;
        this.reservedBy = null;
        this.reservedAt = null;
        this.finalPrice = null;
    }
}
