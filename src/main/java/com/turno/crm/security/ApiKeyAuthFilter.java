package com.turno.crm.security;

import com.turno.crm.exception.RateLimitExceededException;
import com.turno.crm.model.entity.WebhookApiKey;
import com.turno.crm.repository.WebhookApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final String API_KEY_HEADER = "X-API-Key";

    private final WebhookApiKeyRepository webhookApiKeyRepository;
    private final Map<String, Deque<Long>> requestTimes = new ConcurrentHashMap<>();

    public ApiKeyAuthFilter(WebhookApiKeyRepository webhookApiKeyRepository) {
        this.webhookApiKeyRepository = webhookApiKeyRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/api/v1/webhook/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (!StringUtils.hasText(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing X-API-Key header\"}");
            return;
        }

        String keyHash = sha256(apiKey);

        // Rate limiting
        if (isRateLimited(keyHash)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Max " + MAX_REQUESTS_PER_MINUTE + " requests per minute.\"}");
            return;
        }

        // Validate key
        Optional<WebhookApiKey> optKey = webhookApiKeyRepository.findByKeyHashAndActiveTrue(keyHash);

        if (optKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid API key\"}");
            return;
        }

        // Set authentication in SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "webhook-api-key:" + optKey.get().getId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_WEBHOOK"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String keyHash) {
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000; // 1 minute window

        Deque<Long> times = requestTimes.computeIfAbsent(keyHash, k -> new ArrayDeque<>());

        synchronized (times) {
            // Remove entries older than the window
            while (!times.isEmpty() && times.peekFirst() < windowStart) {
                times.pollFirst();
            }

            if (times.size() >= MAX_REQUESTS_PER_MINUTE) {
                return true;
            }

            times.addLast(now);
            return false;
        }
    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void cleanupExpiredEntries() {
        long cutoff = System.currentTimeMillis() - 60_000;
        requestTimes.entrySet().forEach(entry -> {
            synchronized (entry.getValue()) {
                while (!entry.getValue().isEmpty() && entry.getValue().peekFirst() < cutoff) {
                    entry.getValue().pollFirst();
                }
            }
        });
        requestTimes.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
