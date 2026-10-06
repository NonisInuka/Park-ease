package com.se1020.vehicleparking.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Password recovery, implemented in a way that is safe for local development / a university
 * demo: no SMTP/SMS integration exists yet (that is explicitly out of scope - see the proposal's
 * "Email and SMS Notifications" minor function, deferred to a later phase), so instead of
 * emailing a reset link, the generated link is shown directly on the confirmation page and
 * logged to the console. Tokens are kept in memory only (not persisted to SQL Server), are
 * single-use, and expire after 30 minutes. This intentionally does not touch the SQL Server
 * schema from Phase 1.
 */
@Service
public class PasswordResetService {

    private static final long TOKEN_TTL_MINUTES = 30;

    private final Map<String, ResetToken> tokensByValue = new ConcurrentHashMap<>();

    public String createToken(String userId) {
        // Invalidate any previous outstanding token(s) for this user first.
        tokensByValue.values().removeIf(t -> t.userId.equals(userId));

        String token = UUID.randomUUID().toString();
        tokensByValue.put(token, new ResetToken(userId, Instant.now().plusSeconds(TOKEN_TTL_MINUTES * 60)));
        return token;
    }

    /** Returns the userId for a still-valid token, or null if the token is missing/expired. */
    public String resolveUserId(String token) {
        if (token == null) {
            return null;
        }
        ResetToken t = tokensByValue.get(token);
        if (t == null) {
            return null;
        }
        if (Instant.now().isAfter(t.expiresAt)) {
            tokensByValue.remove(token);
            return null;
        }
        return t.userId;
    }

    /** Call once the password has actually been reset, so the token cannot be reused. */
    public void consumeToken(String token) {
        if (token != null) {
            tokensByValue.remove(token);
        }
    }

    private static final class ResetToken {
        final String userId;
        final Instant expiresAt;

        ResetToken(String userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }
}
