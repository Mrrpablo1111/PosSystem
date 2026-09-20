package com.sh.sh.pos.system.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.security.RolePermissions;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class JwtValidator extends OncePerRequestFilter {

    /** Built once, not per request */
    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(JwtConstant.JWT_SECRET.getBytes());

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(KEY)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

                String email = claims.getSubject();
                String authoritiesClaim = String.valueOf(claims.get("authorities"));
                log.debug("Authenticated JWT for subject={}", email);

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        email, null, buildAuthorities(authoritiesClaim));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (ExpiredJwtException e) {
                // Do NOT throw — exceptions here bypass the entry point and
                // become 500s. Continue unauthenticated; the .authenticated()
                // rule downstream produces a clean 401 via the entry point.
                log.debug("Expired JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                log.warn("Invalid JWT rejected: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Turns the token's authority list (e.g. "ROLE_WAREHOUSE_MANAGER") into
     * the full authority set: the role itself PLUS every permission that role
     * grants via RolePermissions.
     *
     * Expanding here — at validation time, not at token creation — means
     * editing RolePermissions takes effect on the very next request, without
     * waiting for tokens to expire or forcing re-login. It also keeps JWTs
     * small: the token carries one role, not fifty permission strings.
     */
    private List<GrantedAuthority> buildAuthorities(String authoritiesClaim) {
        Set<GrantedAuthority> result = new LinkedHashSet<>();   // dedupes

        for (String authority : authoritiesClaim.split(",")) {
            String name = authority.trim();
            if (name.isEmpty()) continue;

            result.add(new SimpleGrantedAuthority(name));       // the role itself

            toUserRole(name).ifPresent(role ->
                RolePermissions.permissionsFor(role)
                        .forEach(p -> result.add(new SimpleGrantedAuthority(p))));
        }
        return List.copyOf(result);
    }

    private java.util.Optional<UserRole> toUserRole(String name) {
        try {
            return java.util.Optional.of(UserRole.valueOf(name));
        } catch (IllegalArgumentException e) {
            return java.util.Optional.empty();   // not a role — keep as plain authority
        }
    }
}
