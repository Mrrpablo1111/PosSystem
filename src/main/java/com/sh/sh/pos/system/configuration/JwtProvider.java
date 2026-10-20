package com.sh.sh.pos.system.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exception.InvalidTokenException;
import com.sh.sh.pos.system.security.UserPrincipal;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class JwtProvider {
    static SecretKey key = Keys.hmacShaKeyFor(
            JwtConstant.JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    private static final long ACCESS_TOKEN_TTL_MS = 24L * 60 * 60 * 1000;

    private static final long REFRESH_TOKEN_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    public JwtProvider() {

        this.key = Keys.hmacShaKeyFor(
                JwtConstant.JWT_SECRET.getBytes(
                        StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + ACCESS_TOKEN_TTL_MS);

        String roles = populateAuthorities(authorities);

        Long userId = getUserId(authentication);

        String email = getEmail(authentication);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)

                .claim("type", "ACCESS")
                .claim("email", email)
                .claim("authorities", roles)

                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + REFRESH_TOKEN_TTL_MS);

        Long userId = getUserId(authentication);
        String email = getEmail(authentication);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)

                .claim("type", "REFRESH")
                .claim("email", email)

                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
      return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getEmailFromToken(String token) {
        return (String) parseClaims(token).get("email");

    }

    public String getTokenType(String token) {
        // Claims claims = Jwts.parser()
        // .verifyWith(key)
        // .build()
        // .parseSignedClaims(jwt)
        // .getPayload();

        return parseClaims(token).get("type", String.class);
    }

    // Rdis-blacklist
    public long getRemainingExpiration(String token) {

        // Claims claims = Jwts.parser()
        // .verifyWith(key)
        // .build()
        // .parseSignedClaims(token)
        // .getPayload();

        Date expiration = parseClaims(token).getExpiration();

        long remaining = expiration.getTime() - System.currentTimeMillis();

        return Math.max(remaining, 0);
    }

    private static String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auths = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            auths.add(authority.getAuthority());
        }
        return String.join(",", auths);
    }

    // ValidateAccessToken
    public boolean validateAccessToken(String token) {

        try {
            Claims claims = parseClaims(token);
            return "ACCESS".equals(claims.get("type"));
        } catch (Exception e) {
            log.warn("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    // ValidateRefreshToken
    public boolean validateRefreshToken(String token) {

        try {
            Claims claims = parseClaims(token);
            return "REFRESH".equals(claims.get("type"));
        } catch (Exception e) {
            log.debug("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Token is missing");
        }

        String cleanToken = token.trim();

        if (cleanToken.startsWith("Bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
        }
        try {

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(cleanToken)
                    .getPayload();

        } catch (Exception e) {

            throw new InvalidTokenException(
                    "Invalid or expired token");
        }
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            throw new InvalidTokenException("Authentication is missing");
        }
        String name = authentication.getName();
        log.info( "JWT DEBUG → authentication.getName()={}, principalType={}", name, authentication.getPrincipal().getClass().getName() );
        if (name == null || name.isBlank()) {
            throw new InvalidTokenException("User ID is missing from authentication");
        }
        try {
            return Long.valueOf(name);
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("Authentication name is not a valid user ID");
        }
    }

    private String getEmail(Authentication authentication) {
        if (authentication == null) {
            throw new InvalidTokenException("Authentication is missing");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getEmail();
        }
        throw new InvalidTokenException("Unable to get user email from authentication");
    }
}