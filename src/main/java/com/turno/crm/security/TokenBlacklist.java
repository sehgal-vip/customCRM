package com.turno.crm.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, long expiryMs) {
        blacklistedTokens.put(token, expiryMs);
        cleanup();
    }

    public boolean isBlacklisted(String token) {
        Long expiry = blacklistedTokens.get(token);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(e -> now > e.getValue());
    }
}
