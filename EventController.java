package com.ticketbooking.booking;

import com.ticketbooking.domain.EventSeat;
import com.ticketbooking.pricing.DynamicPricingService;
import com.ticketbooking.pricing.InventoryCacheService;
import com.ticketbooking.pricing.InventoryCacheService.InventorySnapshot;
import com.ticketbooking.repository.RedisSeatRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final RedisSeatRepository seatRepository;
    private final DynamicPricingService pricingService;
    private final InventoryCacheService inventoryCacheService;

    public EventController(RedisSeatRepository seatRepository,
                           DynamicPricingService pricingService,
                           InventoryCacheService inventoryCacheService) {
        this.seatRepository = seatRepository;
        this.pricingService = pricingService;
        this.inventoryCacheService = inventoryCacheService;
    }

    @GetMapping
    public List<EventDetails> getEvents() {
        return List.of(
            // Concerts
            new EventDetails(
                UUID.fromString("a8b0938f-d124-4f40-8b63-fb71de43026f"),
                "Taylor Swift | The Eras Tour",
                "Experience the pop phenomenon live in an unforgettable stadium show featuring all her musical eras.",
                "November 15, 2026",
                "Wembley Stadium, London",
                "/images/taylor_swift.png",
                "concert", "London"
            ),
            new EventDetails(
                UUID.fromString("c2957bfa-c567-42dc-8656-78b164a2754d"),
                "Coldplay | Music of the Spheres Tour",
                "A spectacular night of lights, color, and classic anthems under the stars.",
                "December 2, 2026",
                "MetLife Stadium, New York",
                "/images/coldplay.png",
                "concert", "New York"
            ),
            new EventDetails(
                UUID.fromString("d688cf8b-eb3f-42e6-a059-86e42b2609bf"),
                "Billie Eilish | Hit Me Hard and Soft Tour",
                "An intimate, high-energy performance featuring tracks from her acclaimed new album.",
                "October 22, 2026",
                "Madison Square Garden, New York",
                "/images/billie_eilish.png",
                "concert", "New York"
            ),
            // Movies
            new EventDetails(
                UUID.fromString("e439bb7f-71b0-482a-8ea6-fde29bc83609"),
                "Dune: Part Two",
                "Follow the mythic journey of Paul Atreides as he unites with Chani and the Fremen.",
                "June 28, 2026",
                "BFI IMAX Cinema, London",
                "/images/dune.png",
                "movie", "London"
            ),
            new EventDetails(
                UUID.fromString("a3901b0f-8c34-4b55-a50d-8ea0b555776d"),
                "Spider-Man: Beyond the Spider-Verse",
                "Miles Morales swings back into action across the multiverse in the ultimate final chapter.",
                "July 12, 2026",
                "PVR Icon Theater, Mumbai",
                "/images/spiderman.png",
                "movie", "Mumbai"
            ),
            new EventDetails(
                UUID.fromString("b0017cfa-4d1a-4712-b9cf-2b81d77a83bb"),
                "Oppenheimer",
                "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.",
                "June 29, 2026",
                "AMC Lincoln Square, New York",
                "/images/oppenheimer.png",
                "movie", "New York"
            ),
            new EventDetails(
                UUID.fromString("d99b110a-3c58-45a8-b997-d81ff110037a"),
                "Inception: Special IMAX Screening",
                "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task.",
                "July 5, 2026",
                "TOHO Cinemas, Tokyo",
                "/images/inception.png",
                "movie", "Tokyo"
            ),
            // Plays
            new EventDetails(
                UUID.fromString("f01b3d7a-18e4-4d83-9b88-1fa12228be0a"),
                "Hamlet | The Royal Shakespeare",
                "Shakespeare's monumental tragedy of vengeance, doubt, and betrayal performed live on stage.",
                "July 2, 2026",
                "Royal Shakespeare Theatre, London",
                "/images/hamlet.png",
                "play", "London"
            ),
            new EventDetails(
                UUID.fromString("c9012aab-2287-43cf-bc8a-d99aa2ba8e0b"),
                "The Lion King Broadway",
                "Experience the jaw-dropping artistry, unforgettable music, and heartwarming story live on Broadway.",
                "July 15, 2026",
                "Minskoff Theatre, New York",
                "/images/lion_king.png",
                "play", "New York"
            ),
            new EventDetails(
                UUID.fromString("a9918cba-091a-4632-a00d-f88a6d71b3e9"),
                "Wicked The Musical",
                "The sensational story of two unlikely friends in the Land of Oz before Dorothy's arrival.",
                "August 1, 2026",
                "Nita Mukesh Ambani Cultural Centre, Mumbai",
                "/images/wicked.png",
                "play", "Mumbai"
            ),
            // Sports
            new EventDetails(
                UUID.fromString("c88e9dcb-b283-4903-b09e-711bc2ba8f81"),
                "Wimbledon Tennis Finals 2026",
                "Watch the world's elite tennis players battle for supremacy on the historic grass courts of London.",
                "July 10, 2026",
                "Centre Court, London",
                "/images/wimbledon.png",
                "sport", "London"
            ),
            new EventDetails(
                UUID.fromString("d1123bf0-19ef-4c12-9bd8-47712bac811a"),
                "NBA Finals Game 7",
                "Witness history in the making as the top basketball champions compete in a winner-takes-all game.",
                "June 27, 2026",
                "Barclays Center, New York",
                "/images/nba.png",
                "sport", "New York"
            ),
            new EventDetails(
                UUID.fromString("e77a11bf-8ef4-4a2a-b092-2bac831bf92a"),
                "T20 Cricket Premier League Finals",
                "Experience the electrical atmosphere as the top cricket franchises clash for the championship trophy.",
                "June 30, 2026",
                "Wankhede Stadium, Mumbai",
                "/images/cricket.png",
                "sport", "Mumbai"
            ),
            // Activities
            new EventDetails(
                UUID.fromString("f662a8c0-8efb-4e12-b883-9e1bf003bb11"),
                "Stargazing & Telescope Night Tour",
                "Explore planets, constellations, and distant galaxies under the guidance of expert astronomers.",
                "July 4, 2026",
                "Royal Observatory Greenwich, London",
                "/images/stargazing.png",
                "activity", "London"
            ),
            new EventDetails(
                UUID.fromString("a22b7cfa-33cf-41c2-901d-bfa2ba99cf02"),
                "Pottery & Wine Crafting Workshop",
                "Mold your own clay masterpiece while enjoying premium vintage wines in a cozy studio setting.",
                "July 8, 2026",
                "Clay & Sip Studio, Mumbai",
                "/images/pottery.png",
                "activity", "Mumbai"
            ),
            new EventDetails(
                UUID.fromString("c33b8dfa-8cfa-4da2-bcf8-d3bc8f1ab211"),
                "Tokyo Street Food Midnight Tour",
                "Savor fresh sushi, yakitori, and local delicacies as you explore Tokyo's secret alleyways.",
                "July 11, 2026",
                "Shibuya Food Alleys, Tokyo",
                "/images/street_food.png",
                "activity", "Tokyo"
            )
        );
    }

    @GetMapping("/{eventId}/seats")
    public List<SeatDetailsResponse> getSeats(@PathVariable UUID eventId) {
        Set<UUID> seatIds = seatRepository.findAllSeatIds(eventId);
        InventorySnapshot inventory = inventoryCacheService.snapshot(eventId);

        return seatIds.stream()
                .map(seatId -> seatRepository.find(eventId, seatId))
                .filter(Objects::nonNull)
                .map(seat -> {
                    BigDecimal currentPrice = pricingService.calculatePrice(
                            eventId,
                            seat.getBasePrice(),
                            "temp-visitor", // dummy customer to not skew demand
                            inventory.getAvailableRatio()
                    );
                    return SeatDetailsResponse.builder()
                            .seatId(seat.getSeatId())
                            .seatName(seat.getSeatName())
                            .status(seat.getStatus().name())
                            .basePrice(seat.getBasePrice())
                            .currentPrice(currentPrice)
                            .reservedBy(seat.getReservedBy())
                            .finalPrice(seat.getFinalPrice())
                            .build();
                })
                .sorted(Comparator.comparing(SeatDetailsResponse::getSeatName))
                .collect(Collectors.toList());
    }

    @GetMapping("/{eventId}/seats/{seatId}")
    public SeatDetailsResponse getSeat(@PathVariable UUID eventId, @PathVariable UUID seatId) {
        EventSeat seat = seatRepository.find(eventId, seatId);
        if (seat == null) {
            return null;
        }
        InventorySnapshot inventory = inventoryCacheService.snapshot(eventId);
        BigDecimal currentPrice = pricingService.calculatePrice(
                eventId,
                seat.getBasePrice(),
                "temp-visitor",
                inventory.getAvailableRatio()
        );
        return SeatDetailsResponse.builder()
                .seatId(seat.getSeatId())
                .seatName(seat.getSeatName())
                .status(seat.getStatus().name())
                .basePrice(seat.getBasePrice())
                .currentPrice(currentPrice)
                .reservedBy(seat.getReservedBy())
                .finalPrice(seat.getFinalPrice())
                .build();
    }

    public record EventDetails(UUID id, String title, String description, String date, String venue, String imageUrl, String category, String city) {}

    @lombok.Data
    @lombok.Builder
    public static class SeatDetailsResponse {
        private UUID seatId;
        private String seatName;
        private String status;
        private BigDecimal basePrice;
        private BigDecimal currentPrice;
        private String reservedBy;
        private BigDecimal finalPrice;
    }
}
