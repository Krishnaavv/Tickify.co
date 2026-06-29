package com.ticketbooking.repository;

import com.ticketbooking.domain.EventSeat;
import com.ticketbooking.domain.SeatStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RedisSeatRepository {

    private final StringRedisTemplate redisTemplate;

    public RedisSeatRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getSeatKey(UUID eventId, UUID seatId) {
        return "seat:event:" + eventId + ":seat:" + seatId;
    }

    private String getSeatIndexKey(UUID eventId) {
        return "seat:index:event:" + eventId;
    }

    public EventSeat find(UUID eventId, UUID seatId) {
        String key = getSeatKey(eventId, seatId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return null;
        }

        return EventSeat.builder()
                .eventId(eventId)
                .seatId(seatId)
                .seatName((String) entries.get("seatName"))
                .status(SeatStatus.valueOf((String) entries.get("status")))
                .basePrice(new BigDecimal((String) entries.get("basePrice")))
                .reservedBy((String) entries.get("reservedBy"))
                .reservedAt(entries.containsKey("reservedAt") && entries.get("reservedAt") != null 
                        ? Instant.parse((String) entries.get("reservedAt")) : null)
                .finalPrice(entries.containsKey("finalPrice") && entries.get("finalPrice") != null 
                        ? new BigDecimal((String) entries.get("finalPrice")) : null)
                .build();
    }

    public void save(EventSeat seat) {
        String key = getSeatKey(seat.getEventId(), seat.getSeatId());
        String indexKey = getSeatIndexKey(seat.getEventId());

        redisTemplate.opsForHash().put(key, "status", seat.getStatus().name());
        redisTemplate.opsForHash().put(key, "basePrice", seat.getBasePrice().toPlainString());
        if (seat.getSeatName() != null) {
            redisTemplate.opsForHash().put(key, "seatName", seat.getSeatName());
        } else {
            redisTemplate.opsForHash().delete(key, "seatName");
        }
        if (seat.getReservedBy() != null) {
            redisTemplate.opsForHash().put(key, "reservedBy", seat.getReservedBy());
        } else {
            redisTemplate.opsForHash().delete(key, "reservedBy");
        }
        if (seat.getReservedAt() != null) {
            redisTemplate.opsForHash().put(key, "reservedAt", seat.getReservedAt().toString());
        } else {
            redisTemplate.opsForHash().delete(key, "reservedAt");
        }
        if (seat.getFinalPrice() != null) {
            redisTemplate.opsForHash().put(key, "finalPrice", seat.getFinalPrice().toPlainString());
        } else {
            redisTemplate.opsForHash().delete(key, "finalPrice");
        }

        redisTemplate.opsForSet().add(indexKey, seat.getSeatId().toString());
    }

    public void reserve(UUID eventId, UUID seatId, String customerId, Instant now, BigDecimal finalPrice) {
        EventSeat seat = find(eventId, seatId);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + seatId);
        }
        seat.reserve(customerId, now, finalPrice);
        save(seat);
    }

    public void confirm(UUID eventId, UUID seatId) {
        EventSeat seat = find(eventId, seatId);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found: " + seatId);
        }
        seat.confirm();
        save(seat);
    }

    public void release(UUID eventId, UUID seatId) {
        EventSeat seat = find(eventId, seatId);
        if (seat == null) {
            return; // Already deleted/non-existent
        }
        seat.release();
        save(seat);
    }

    public Set<UUID> findAllSeatIds(UUID eventId) {
        String indexKey = getSeatIndexKey(eventId);
        Set<String> members = redisTemplate.opsForSet().members(indexKey);
        if (members == null) {
            return Set.of();
        }
        return members.stream().map(UUID::fromString).collect(Collectors.toSet());
    }
}
