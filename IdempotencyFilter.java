package com.ticketbooking.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Component
public class IdempotencyFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Apply only to POST /api/checkout
        if (!"POST".equalsIgnoreCase(httpRequest.getMethod()) || !httpRequest.getRequestURI().endsWith("/api/checkout")) {
            chain.doFilter(request, response);
            return;
        }

        String retryId = httpRequest.getHeader("X-Retry-ID");
        if (retryId == null || retryId.trim().isEmpty()) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write("{\"error\": \"X-Retry-ID header is required for checkout\"}");
            return;
        }

        String key = "idem:checkout:" + retryId;
        String cachedVal = redisTemplate.opsForValue().get(key);

        if (cachedVal != null) {
            if ("IN_FLIGHT".equals(cachedVal)) {
                // Wait and poll once for completion
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                cachedVal = redisTemplate.opsForValue().get(key);
                if (cachedVal == null || "IN_FLIGHT".equals(cachedVal)) {
                    httpResponse.setStatus(HttpStatus.CONFLICT.value());
                    httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    httpResponse.getWriter().write("{\"error\": \"Request with X-Retry-ID is still processing\"}");
                    return;
                }
            }

            // Replay cached response
            try {
                CachedResponse cachedResponse = objectMapper.readValue(cachedVal, CachedResponse.class);
                httpResponse.setStatus(cachedResponse.getStatusCode());
                httpResponse.setContentType(cachedResponse.getContentType());
                httpResponse.getWriter().write(cachedResponse.getBody());
                return;
            } catch (Exception e) {
                // Fallback: clear bad cache and proceed
                redisTemplate.delete(key);
            }
        }

        // Attempt to set as IN_FLIGHT
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "IN_FLIGHT", Duration.ofMinutes(2));
        if (success == null || !success) {
            httpResponse.setStatus(HttpStatus.CONFLICT.value());
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write("{\"error\": \"Request with X-Retry-ID is still processing\"}");
            return;
        }

        // Wrap response to capture output
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        try {
            chain.doFilter(request, wrappedResponse);

            // Fetch the status and response body
            int statusCode = wrappedResponse.getStatus();
            String contentType = wrappedResponse.getContentType();
            String responseBody = new String(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding());

            // If success (2xx) or specific errors, cache the result
            if (statusCode >= 200 && statusCode < 300) {
                CachedResponse cachePayload = new CachedResponse(statusCode, contentType, responseBody);
                String serialized = objectMapper.writeValueAsString(cachePayload);
                redisTemplate.opsForValue().set(key, serialized, Duration.ofMinutes(2));
            } else {
                // On server failure (5xx) or other transient errors, remove IN_FLIGHT so client can retry
                redisTemplate.delete(key);
            }

            // Write back to the original response
            wrappedResponse.copyBodyToResponse();
        } catch (Throwable t) {
            redisTemplate.delete(key);
            throw t;
        }
    }

    public static class CachedResponse {
        private int statusCode;
        private String contentType;
        private String body;

        public CachedResponse() {}

        public CachedResponse(int statusCode, String contentType, String body) {
            this.statusCode = statusCode;
            this.contentType = contentType;
            this.body = body;
        }

        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }
}
