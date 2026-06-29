package com.ticketbooking.booking;

import com.ticketbooking.domain.EventSeat;
import com.ticketbooking.domain.SeatStatus;
import com.ticketbooking.pricing.InventoryCacheService;
import com.ticketbooking.repository.RedisSeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/seats/{seatId}")
public class SeedController {

    private final RedisSeatRepository seatRepository;
    private final InventoryCacheService inventoryCacheService;

    public SeedController(RedisSeatRepository seatRepository, InventoryCacheService inventoryCacheService) {
        this.seatRepository = seatRepository;
        this.inventoryCacheService = inventoryCacheService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> seedSeat(
            @PathVariable UUID eventId,
            @PathVariable UUID seatId,
            @RequestParam BigDecimal basePrice) {

        EventSeat seat = EventSeat.builder()
                .eventId(eventId)
                .seatId(seatId)
                .status(SeatStatus.OPEN)
                .basePrice(basePrice)
                .build();

        seatRepository.save(seat);

        // Retrieve total seat count for this event to update the inventory cache
        Set<UUID> allSeatIds = seatRepository.findAllSeatIds(eventId);
        inventoryCacheService.initialize(eventId, allSeatIds.size());

        return Map.of(
                "eventId", eventId.toString(),
                "seatId", seatId.toString(),
                "status", SeatStatus.OPEN.name(),
                "basePrice", basePrice.toPlainString(),
                "totalEventSeatsCached", String.valueOf(allSeatIds.size())
        );
    }
}
