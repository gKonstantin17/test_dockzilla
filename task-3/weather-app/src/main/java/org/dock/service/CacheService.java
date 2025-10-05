package org.dock.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService {
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final int CACHE_TTL_MINUTES = 15;

    public void put(String key, String value) {
        cache.put(key, new CacheEntry(value, LocalDateTime.now().plusMinutes(CACHE_TTL_MINUTES)));
    }

    public String get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        // Check if entry is expired
        if (LocalDateTime.now().isAfter(entry.expiryTime)) {
            cache.remove(key);
            return null;
        }

        return entry.value;
    }

    private static class CacheEntry {
        private final String value;
        private final LocalDateTime expiryTime;

        public CacheEntry(String value, LocalDateTime expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
    }
}
