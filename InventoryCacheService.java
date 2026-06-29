package com.ticketbooking.pricing;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class InventoryCacheService {

    private final StringRedisTemplate redisTemplate;

    public InventoryCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getInventoryKey(UUID eventId) {
        return "inventory:event:" + eventId;
    }

    public void initialize(UUID eventId, int totalSeats) {
        String key = getInventoryKey(eventId);
        redisTemplate.opsForHash().put(key, "total", String.valueOf(totalSeats));
        redisTemplate.opsForHash().put(key, "reserved", "0");
    }

    public InventorySnapshot snapshot(UUID eventId) {
        String key = getInventoryKey(eventId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return new InventorySnapshot(100, 0); // Default fallback values
        }

        int total = Integer.parseInt((String) entries.getOrDefault("total", "100"));
        int reserved = Integer.parseInt((String) entries.getOrDefault("reserved", "0"));
        return new InventorySnapshot(total, reserved);
    }

    public void seatReserved(UUID eventId) {
        String key = getInventoryKey(eventId);
        redisTemplate.opsForHash().increment(key, "reserved", 1);
    }

    public void seatReleased(UUID eventId) {
        String key = getInventoryKey(eventId);
        redisTemplate.opsForHash().increment(key, "reserved", -1);
    }

    public record InventorySnapshot(int total, int reserved) {
        public double getAvailableRatio() {
            if (total == 0) return 0.0;
            return (double) (total - reserved) / total;
        }
    }
}
