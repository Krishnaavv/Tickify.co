package com.ticketbooking.pricing;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DynamicPricingService {

    private final StringRedisTemplate redisTemplate;

    public DynamicPricingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public BigDecimal calculatePrice(UUID eventId, BigDecimal basePrice, String customerId, double availableRatio) {
        // 1. Record demand for the current customer
        recordDemand(eventId, customerId);

        // 2. Fetch cached multiplier
        String multiplierKey = "price:multiplier:event:" + eventId;
        String cachedMultiplier = redisTemplate.opsForValue().get(multiplierKey);
        BigDecimal multiplier;

        if (cachedMultiplier != null) {
            multiplier = new BigDecimal(cachedMultiplier);
        } else {
            // Cache miss: calculate new multiplier
            long demand = getRecentDemand(eventId);
            double demandPressure = Math.min(1.0, (double) demand / 250.0);
            double scarcityPressure = Math.max(0.0, 1.0 - availableRatio);

            double multiplierValue = 1.0 + (demandPressure * 0.85) + (scarcityPressure * 1.15);
            multiplierValue = Math.min(3.0, multiplierValue); // Cap at 3.0x

            multiplier = BigDecimal.valueOf(multiplierValue).setScale(2, RoundingMode.HALF_UP);

            // Cache in Redis for 20 seconds
            redisTemplate.opsForValue().set(multiplierKey, multiplier.toPlainString(), Duration.ofSeconds(20));
        }

        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMultiplier(UUID eventId) {
        String multiplierKey = "price:multiplier:event:" + eventId;
        String cached = redisTemplate.opsForValue().get(multiplierKey);
        if (cached != null) {
            return new BigDecimal(cached);
        }
        return BigDecimal.ONE;
    }

    private void recordDemand(UUID eventId, String customerId) {
        if (customerId == null || customerId.trim().isEmpty() || "temp-visitor".equals(customerId)) {
            return;
        }
        long currentBucket = Instant.now().getEpochSecond() / 60;
        String key = "price:demand:event:" + eventId + ":minute:" + currentBucket;
        
        redisTemplate.opsForHyperLogLog().add(key, customerId);
        // Expire HLL bucket after 6 minutes (360 seconds) to save space
        redisTemplate.expire(key, Duration.ofMinutes(6));
    }

    private long getRecentDemand(UUID eventId) {
        long currentBucket = Instant.now().getEpochSecond() / 60;
        List<String> keys = new ArrayList<>();
        // Look back at the last 5 minutes of buckets
        for (int i = 0; i < 5; i++) {
            keys.add("price:demand:event:" + eventId + ":minute:" + (currentBucket - i));
        }
        
        Long count = redisTemplate.opsForHyperLogLog().size(keys.toArray(new String[0]));
        return count != null ? count : 0L;
    }
}
