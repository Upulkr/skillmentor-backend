package com.skillmentor.security;

import com.skillmentor.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT PROVIDER
 * 
 * Handles token parsing and verification using Clerk's JWKS or PEM.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class JwtProvider {

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.clerk.pem:}")
    private String clerkPem;

    @Value("${app.clerk.jwks-url:}")
    private String jwksUrl;

    private final Map<String, java.security.Key> keyCache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Validate JWT token
     */
    public Claims validateToken(String token) {
        try {
            java.security.Key key = getVerificationKey(token);

            var parserBuilder = Jwts.parser();

            if (key instanceof javax.crypto.SecretKey) {
                parserBuilder.verifyWith((javax.crypto.SecretKey) key);
            } else if (key instanceof java.security.PublicKey) {
                parserBuilder.verifyWith((java.security.PublicKey) key);
            }

            return parserBuilder
                    .clockSkewSeconds(604800) // Allow 1 week of clock skew (for development)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid JWT token: " + e.getMessage());
        }
    }

    /**
     * Extract User ID (subject)
     */
    public String getUserIdFromToken(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extract Role
     */
    @SuppressWarnings("unchecked")
    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        log.debug("JWT Claims: {}", claims);

        // 1. Check direct 'role' claim (from Clerk JWT Template)
        String role = claims.get("role", String.class);
        if (role != null)
            return role.toUpperCase();

        // 2. Check Clerk 'public_metadata'
        Map<String, Object> metadata = claims.get("public_metadata", Map.class);
        if (metadata != null) {
            Object rolesObj = metadata.get("roles");
            if (rolesObj instanceof List) {
                List<String> rolesList = (List<String>) rolesObj;
                if (!rolesList.isEmpty())
                    return rolesList.get(0).toUpperCase();
            }
            Object roleObj = metadata.get("role");
            if (roleObj != null)
                return roleObj.toString().toUpperCase();
        }

        // 3. Fallback: Check Clerk Organization Role ('o' claim)
        Map<String, Object> orgMetadata = claims.get("o", Map.class);
        if (orgMetadata != null && orgMetadata.get("rol") != null) {
            String orgRole = orgMetadata.get("rol").toString().toUpperCase();
            log.info("Found organization role: {}", orgRole);
            return orgRole;
        }

        log.warn("No role found in JWT for user: {}, defaulting to STUDENT", claims.getSubject());
        return "STUDENT"; // Default
    }

    private java.security.Key getVerificationKey(String token) {
        // 1. Try JWKS if URL is provided
        if (jwksUrl != null && !jwksUrl.isBlank()) {
            try {
                return resolveKeyFromJwks(token);
            } catch (Exception e) {
                // Log and fallback to PEM
            }
        }

        // 2. Try PEM
        if (clerkPem != null && !clerkPem.isBlank()) {
            return decodePublicKey(clerkPem);
        }

        // 3. Fallback to HMAC
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }

        throw new RuntimeException("No JWT verification key/url provided!");
    }

    private java.security.Key resolveKeyFromJwks(String token) {
        try {
            // Extract 'kid' from header without verification
            String[] chunks = token.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));

            // Minimal JSON parsing to find 'kid'
            String kid = null;
            if (headerJson.contains("\"kid\":\"")) {
                int start = headerJson.indexOf("\"kid\":\"") + 7;
                int end = headerJson.indexOf("\"", start);
                kid = headerJson.substring(start, end);
            }

            if (kid == null)
                throw new RuntimeException("No kid in token header");

            if (keyCache.containsKey(kid))
                return keyCache.get(kid);

            // Fetch JWKS
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(jwksUrl, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> keys = (List<Map<String, Object>>) response.get("keys");

            for (Map<String, Object> keyData : keys) {
                String k = (String) keyData.get("kid");
                String n = (String) keyData.get("n");
                String e = (String) keyData.get("e");

                RSAPublicKeySpec spec = new RSAPublicKeySpec(
                        new BigInteger(1, Base64.getUrlDecoder().decode(n)),
                        new BigInteger(1, Base64.getUrlDecoder().decode(e)));
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
                keyCache.put(k, publicKey);
            }

            return keyCache.get(kid);
        } catch (Exception e) {
            throw new RuntimeException("JWKS Resolution failed: " + e.getMessage());
        }
    }

    private java.security.PublicKey decodePublicKey(String pem) {
        try {
            String key = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "")
                    .replace("\\n", "");
            byte[] encoded = Base64.getDecoder().decode(key);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new RuntimeException("PEM decoding failed: " + e.getMessage());
        }
    }
}
