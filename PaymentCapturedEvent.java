package com.ticketbooking.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCapturedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID reservationId;
    private UUID eventId;
    private UUID seatId;
    private String customerId;
    private BigDecimal amount;
    private String transactionId;
}
