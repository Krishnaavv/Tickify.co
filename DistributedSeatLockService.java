package com.ticketbooking.locking;

import com.ticketbooking.exception.SeatLockUnavailableException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class DistributedSeatLockService {

    private final RedissonClient redissonClient;
    private final long leaseMs;

    public DistributedSeatLockService(RedissonClient redissonClient, 
                                      @Value("${booking.lock.lease-ms:300000}") long leaseMs) {
        this.redissonClient = redissonClient;
        this.leaseMs = leaseMs;
    }

    public <T> T withSeatLock(UUID eventId, UUID seatId, Supplier<T> action) {
        String lockKey = "lock:event:" + eventId + ":seat:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired;
        try {
            // tryLock(waitTime, leaseTime, unit)
            acquired = lock.tryLock(0, leaseMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted", e);
        }

        if (!acquired) {
            throw new SeatLockUnavailableException(eventId, seatId);
        }

        try {
            return action.get();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void releaseLock(UUID eventId, UUID seatId) {
        String lockKey = "lock:event:" + eventId + ":seat:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
        }
    }
}
