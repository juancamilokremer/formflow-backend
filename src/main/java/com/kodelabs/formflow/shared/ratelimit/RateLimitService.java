package com.kodelabs.formflow.shared.ratelimit;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding-window rate limiter. No persistence — restarts reset counts.
 * Suitable for single-instance deployments (Railway free tier).
 */
@Component
public class RateLimitService {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentHashMap<String, Deque<Long>> timestamps = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> deque = timestamps.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < now - WINDOW_MILLIS) {
                deque.pollFirst();
            }
            if (deque.size() >= MAX_REQUESTS) return false;
            deque.addLast(now);
            return true;
        }
    }
}
