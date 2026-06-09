package com.school.application.platform.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtilService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // Converts our Base64 secret string into a cryptographic key used for signing
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    // Builds and signs a JWT. The "subject" is the user's email — it's
    // how we know who this token belongs to when it comes back on future requests.
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    // Reads the email out of a token's payload (the "subject" claim we set above)
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key()).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Parses the token — if it's expired, malformed, or has a bad signature,
    // an exception is thrown and we return false. Otherwise the token is valid.
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}