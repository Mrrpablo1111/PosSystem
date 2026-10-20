package com.sh.sh.pos.system.configuration;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.security.UserPrincipal;
import com.sh.sh.pos.system.service.redisservice.JwtBlackListService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidator extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;

    private final JwtBlackListService jwtBlackListService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header =
                request.getHeader(JwtConstant.JWT_HEADER);

     
        //  No Authorization header

        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Must be Bearer token


        if (!header.startsWith("Bearer ")) {

            log.debug(
                    "Invalid Authorization header format → {} {}",
                    request.getMethod(),
                    request.getRequestURI()
            );

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                header.substring(7).trim();

        if (token.isBlank()) {

            filterChain.doFilter(request, response);
            return;
        }

        try {

            Authentication currentAuthentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (currentAuthentication != null
                    && currentAuthentication.isAuthenticated()) {

                filterChain.doFilter(request, response);
                return;
            }


            //  Validate ACCESS token
            // JwtProvider checks:
            // - signature
            // - expiration
            // - token type
         

            if (!jwtProvider.validateAccessToken(token)) {

                log.debug(
                        "Invalid or expired access token"
                );

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            // 5. Check JWT blacklist
            // Logout puts the access token into Redis blacklist.
          

            if (jwtBlackListService.isBlacklisted(token)) {

                log.debug(
                        "Rejected blacklisted JWT"
                );

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }


            // Get user ID from JWT subject           

            Long userId =
                    jwtProvider.getUserIdFromToken(token);

        
            // Load current user from database
            // Do NOT trust role/authority from JWT.

            User user =
                    userRepository.findById(userId)
                            .orElse(null);

            if (user == null) {

                log.debug(
                        "User not found for JWT → userId={}",
                        userId
                );

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }


            // Check user status
            // Add more account-status rules here later if your
            // User entity gets fields such as:
            // enabled
            // locked
            // verified
            // deleted


            if (Boolean.FALSE.equals(user.getVerified())) {

                log.debug(
                        "User is not verified → userId={}",
                        userId
                );

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            // =====================================================
            // 9. Create UserPrincipal
            //
            // UserPrincipal gets the current role from User.
            // =====================================================

            UserPrincipal userPrincipal =
                    new UserPrincipal(user);

            // =====================================================
            // 10. Create Spring Security Authentication
            // =====================================================

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                    );

            // =====================================================
            // 11. Store authentication
            // =====================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            log.debug(
                    "JWT authentication successful → userId={} role={}",
                    user.getId(),
                    user.getRole()
            );

        } catch (Exception e) {

            // =====================================================
            // 12. Never keep invalid authentication
            // =====================================================

            SecurityContextHolder.clearContext();

            log.debug(
                    "JWT authentication failed → {}",
                    e.getMessage()
            );
        }

        // =========================================================
        // 13. Continue request
        // =========================================================

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
   
}
