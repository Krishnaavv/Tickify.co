package com.ticketbooking.booking;

import com.ticketbooking.domain.EventSeat;
import com.ticketbooking.domain.SeatStatus;
import com.ticketbooking.pricing.InventoryCacheService;
import com.ticketbooking.repository.RedisSeatRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DataSeeder implements ApplicationRunner {

    private final RedisSeatRepository seatRepository;
    private final InventoryCacheService inventoryCacheService;

    // Fixed event IDs for consistency
    public static final Map<UUID, String> EVENTS = Map.ofEntries(
            Map.entry(UUID.fromString("a8b0938f-d124-4f40-8b63-fb71de43026f"), "Taylor Swift | The Eras Tour"),
            Map.entry(UUID.fromString("c2957bfa-c567-42dc-8656-78b164a2754d"), "Coldplay | Music of the Spheres Tour"),
            Map.entry(UUID.fromString("d688cf8b-eb3f-42e6-a059-86e42b2609bf"), "Billie Eilish | Hit Me Hard and Soft Tour"),
            Map.entry(UUID.fromString("e439bb7f-71b0-482a-8ea6-fde29bc83609"), "Dune: Part Two"),
            Map.entry(UUID.fromString("a3901b0f-8c34-4b55-a50d-8ea0b555776d"), "Spider-Man: Beyond the Spider-Verse"),
            Map.entry(UUID.fromString("b0017cfa-4d1a-4712-b9cf-2b81d77a83bb"), "Oppenheimer"),
            Map.entry(UUID.fromString("d99b110a-3c58-45a8-b997-d81ff110037a"), "Inception: Special IMAX Screening"),
            Map.entry(UUID.fromString("f01b3d7a-18e4-4d83-9b88-1fa12228be0a"), "Hamlet | The Royal Shakespeare"),
            Map.entry(UUID.fromString("c9012aab-2287-43cf-bc8a-d99aa2ba8e0b"), "The Lion King Broadway"),
            Map.entry(UUID.fromString("a9918cba-091a-4632-a00d-f88a6d71b3e9"), "Wicked The Musical"),
            Map.entry(UUID.fromString("c88e9dcb-b283-4903-b09e-711bc2ba8f81"), "Wimbledon Tennis Finals 2026"),
            Map.entry(UUID.fromString("d1123bf0-19ef-4c12-9bd8-47712bac811a"), "NBA Finals Game 7"),
            Map.entry(UUID.fromString("e77a11bf-8ef4-4a2a-b092-2bac831bf92a"), "T20 Cricket Premier League Finals"),
            Map.entry(UUID.fromString("f662a8c0-8efb-4e12-b883-9e1bf003bb11"), "Stargazing & Telescope Night Tour"),
            Map.entry(UUID.fromString("a22b7cfa-33cf-41c2-901d-bfa2ba99cf02"), "Pottery & Wine Crafting Workshop"),
            Map.entry(UUID.fromString("c33b8dfa-8cfa-4da2-bcf8-d3bc8f1ab211"), "Tokyo Street Food Midnight Tour")
    );

    public DataSeeder(RedisSeatRepository seatRepository, InventoryCacheService inventoryCacheService) {
        this.seatRepository = seatRepository;
        this.inventoryCacheService = inventoryCacheService;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (UUID eventId : EVENTS.keySet()) {
            Set<UUID> existingSeats = seatRepository.findAllSeatIds(eventId);
            if (existingSeats.isEmpty()) {
                seedEventSeats(eventId);
            }
        }
    }

    private void seedEventSeats(UUID eventId) {
        String[] rows = {"A", "B", "C", "D", "E", "F"};
        int seatNumberMax = 10;
        int totalSeats = rows.length * seatNumberMax;

        for (String row : rows) {
            BigDecimal basePrice;
            if ("A".equals(row) || "B".equals(row)) {
                basePrice = new BigDecimal("250.00");
            } else if ("C".equals(row) || "D".equals(row)) {
                basePrice = new BigDecimal("120.00");
            } else {
                basePrice = new BigDecimal("75.00");
            }

            for (int i = 1; i <= seatNumberMax; i++) {
                String seatName = row + i;
                // Generate a deterministic UUID based on eventId and seatName
                UUID seatId = UUID.nameUUIDFromBytes((eventId.toString() + ":" + seatName).getBytes());

                EventSeat seat = EventSeat.builder()
                        .eventId(eventId)
                        .seatId(seatId)
                        .seatName(seatName)
                        .status(SeatStatus.OPEN)
                        .basePrice(basePrice)
                        .build();

                seatRepository.save(seat);
            }
        }

        inventoryCacheService.initialize(eventId, totalSeats);
    }
}
