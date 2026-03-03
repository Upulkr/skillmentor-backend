package com.skillmentor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    /**
     * Validate JWT token from Clerk
     * Extracts and returns user claims
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getClerkPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid JWT token");
        }
    }

    /**
     * Extract user ID from JWT
     */
    public String getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Extract role from JWT
     */
    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }

    private Key getClerkPublicKey() {
        // Fetch from Clerk's JWKS endpoint
        // TODO: Implement key fetching
        return null;
    }
}