package com.ticketbooking.booking;

import com.ticketbooking.domain.EventSeat;
import com.ticketbooking.domain.SeatStatus;
import com.ticketbooking.exception.SeatAlreadyAllocatedException;
import com.ticketbooking.exception.SeatNotFoundException;
import com.ticketbooking.locking.DistributedSeatLockService;
import com.ticketbooking.locking.LocalEventLockRegistry;
import com.ticketbooking.pricing.DynamicPricingService;
import com.ticketbooking.pricing.InventoryCacheService;
import com.ticketbooking.pricing.InventoryCacheService.InventorySnapshot;
import com.ticketbooking.repository.RedisSeatRepository;
import com.ticketbooking.saga.SagaEventPublisher;
import com.ticketbooking.common.event.ReservationReservedEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {

    private final LocalEventLockRegistry localLockRegistry;
    private final DistributedSeatLockService distributedLockService;
    private final RedisSeatRepository seatRepository;
    private final DynamicPricingService pricingService;
    private final InventoryCacheService inventoryCacheService;
    private final SagaEventPublisher eventPublisher;

    public BookingService(LocalEventLockRegistry localLockRegistry,
                          DistributedSeatLockService distributedLockService,
                          RedisSeatRepository seatRepository,
                          DynamicPricingService pricingService,
                          InventoryCacheService inventoryCacheService,
                          SagaEventPublisher eventPublisher) {
        this.localLockRegistry = localLockRegistry;
        this.distributedLockService = distributedLockService;
        this.seatRepository = seatRepository;
        this.pricingService = pricingService;
        this.inventoryCacheService = inventoryCacheService;
        this.eventPublisher = eventPublisher;
    }

    public ReservationResponse reserve(ReservationRequest request) {
        UUID eventId = request.getEventId();
        UUID seatId = request.getSeatId();
        String customerId = request.getCustomerId();

        // Tier 1: Local JVM Lock to serialize writes for this event locally
        return localLockRegistry.write(eventId, () -> {
            
            // Tier 2: Distributed Lock per seat
            return distributedLockService.withSeatLock(eventId, seatId, () -> {
                
                // Lookup seat status
                EventSeat seat = seatRepository.find(eventId, seatId);
                if (seat == null) {
                    throw new SeatNotFoundException(eventId, seatId);
                }

                // Domain-level validation
                if (seat.getStatus() != SeatStatus.OPEN) {
                    throw new SeatAlreadyAllocatedException(eventId, seatId);
                }

                // Dynamic pricing evaluation
                InventorySnapshot inventory = inventoryCacheService.snapshot(eventId);
                BigDecimal finalPrice = pricingService.calculatePrice(
                    eventId, 
                    seat.getBasePrice(), 
                    customerId, 
                    inventory.getAvailableRatio()
                );

                // Reserve seat state change
                Instant now = Instant.now();
                seat.reserve(customerId, now, finalPrice);
                seatRepository.save(seat);

                // Update inventory cache
                inventoryCacheService.seatReserved(eventId);

                // Initiate Saga (emit ReservationReservedEvent to Kafka)
                UUID reservationId = UUID.randomUUID();
                ReservationReservedEvent reservedEvent = ReservationReservedEvent.builder()
                        .reservationId(reservationId)
                        .eventId(eventId)
                        .seatId(seatId)
                        .customerId(customerId)
                        .finalPrice(finalPrice)
                        .reservedAt(now)
                        .build();

                eventPublisher.publishReservationReserved(reservedEvent);

                // Return Reservation DTO
                return ReservationResponse.builder()
                        .reservationId(reservationId)
                        .eventId(eventId)
                        .seatId(seatId)
                        .customerId(customerId)
                        .finalPrice(finalPrice)
                        .multiplier(pricingService.getMultiplier(eventId))
                        .reservedAt(now)
                        .build();
            });
        });
    }

    public void releaseSeat(UUID eventId, UUID seatId, String customerId) {
        localLockRegistry.write(eventId, () -> {
            EventSeat seat = seatRepository.find(eventId, seatId);
            if (seat != null && (seat.getStatus() == SeatStatus.RESERVED || seat.getStatus() == SeatStatus.CONFIRMED)) {
                seat.release();
                seatRepository.save(seat);
                inventoryCacheService.seatReleased(eventId);
            }
            // Release the Redisson lock immediately
            distributedLockService.releaseLock(eventId, seatId);
            return null;
        });
    }

    public void confirmSeat(UUID eventId, UUID seatId) {
        localLockRegistry.write(eventId, () -> {
            EventSeat seat = seatRepository.find(eventId, seatId);
            if (seat != null) {
                seat.confirm();
                seatRepository.save(seat);
            }
            return null;
        });
    }
}
