package org.example.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class JwtUtils {
    // Configuration - in production, load from environment variables
    private static final String SECRET_STRING = "MySecretKeyForEventBookingSystemThatIsLongEnoughForHS256Algorithm";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days (matches your original)
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 15;

    // Token blacklist for logout functionality
    private static final Map<String, Long> invalidatedTokens = new ConcurrentHashMap<>();

    // Generate authentication token (your original implementation enhanced)
    public static String generateToken(String email, String role, String name, Date createdAt) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("name", name)
                .claim("createdAt", createdAt.getTime()) // Store as timestamp (your original)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Generate password reset token (enhanced version)
    public static String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "password-reset") // Added type claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (RESET_TOKEN_EXPIRATION_MINUTES * 60 * 1000)))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Validate token (combined both implementations)
    public static boolean validateToken(String token) {
        try {
            if (isTokenInvalidated(token)) {
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    // Validate password reset token (enhanced version)
    public static boolean validatePasswordResetToken(String token, String email) {
        try {
            if (isTokenInvalidated(token)) {
                return false;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if it's a password reset token
            if (!"password-reset".equals(claims.get("type"))) {
                return false;
            }

            return email.equals(claims.getSubject());
        } catch (ExpiredJwtException e) {
            System.err.println("Password reset token expired: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Invalid password reset token: " + e.getMessage());
        }
        return false;
    }

    // Token utilities (your original methods)
    public static String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public static boolean isAdmin(String token) {
        if (!validateToken(token)) return false;
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        return "admin".equals(role);
    }

    // Enhanced claim extraction (your original implementation)
    private static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private static Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Token invalidation (from second implementation)
    public static void invalidateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            invalidatedTokens.put(token, claims.getExpiration().getTime());
            cleanupExpiredInvalidatedTokens();
        } catch (Exception e) {
            System.err.println("Error invalidating token: " + e.getMessage());
        }
    }

    private static boolean isTokenInvalidated(String token) {
        return invalidatedTokens.containsKey(token);
    }

    private static void cleanupExpiredInvalidatedTokens() {
        long currentTime = System.currentTimeMillis();
        invalidatedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }

    // Additional utility methods (from second implementation)
    public static String getRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public static String getNameFromToken(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    public static Date getCreatedAtFromToken(String token) {
        Long timestamp = extractClaim(token, claims -> claims.get("createdAt", Long.class));
        return timestamp != null ? new Date(timestamp) : null;
    }
}