package com.example.security_token.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT secret cannot be empty");
        }

        if (jwtSecret.length() < 32) {
            log.warn("JWT secret is too short - recommended minimum is 32 characters");
        }

        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Authentication cannot be null or not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                // The Stream.toList() method returns an unmodifiable list, while Collectors.toList() typically returns a modifiable ArrayList
                //.collect(Collectors.toList());
                .toList();

        log.warn("authorities" + authorities);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = parseToken(token).getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            parseToken(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Jws<Claims> parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token cannot be empty");
        }

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token.trim());
    }

    // Método adicional para obtener claims
    public Claims getAllClaimsFromToken(String token) {
        return parseToken(token).getBody();
    }
}