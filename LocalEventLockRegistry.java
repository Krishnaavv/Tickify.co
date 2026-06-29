package com.ticketbooking.locking;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.UUID;

@Component
public class LocalEventLockRegistry {

    private final ConcurrentHashMap<UUID, ReentrantReadWriteLock> eventLocks = new ConcurrentHashMap<>();

    public <T> T write(UUID eventId, Supplier<T> action) {
        ReentrantReadWriteLock lock = eventLocks.computeIfAbsent(eventId, ignored -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            return action.get();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
