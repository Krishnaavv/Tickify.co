package com.ticketbooking.booking;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/seats/{seatId}")
public class RetryTokenController {

    @PostMapping("/retry-token")
    public Map<String, String> generateRetryToken(@PathVariable UUID eventId, @PathVariable UUID seatId) {
        String token = "retry-" + eventId + "-" + seatId + "-" + UUID.randomUUID();
        return Map.of("retryToken", token);
    }
}
