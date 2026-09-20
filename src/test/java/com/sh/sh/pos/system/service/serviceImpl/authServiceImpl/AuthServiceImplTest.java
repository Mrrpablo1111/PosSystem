package com.sh.sh.pos.system.service.serviceimpl.authServiceImpl;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.sh.sh.pos.system.configuration.JwtProvider;
import com.sh.sh.pos.system.configuration.emailtemplate.EmailTemplateBuilder;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.response.AuthResponse;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.emailservice.EmailService;
import com.sh.sh.pos.system.service.redisservice.JwtBlackListService;
import com.sh.sh.pos.system.service.redisservice.PasswordResetRedisService;
import com.sh.sh.pos.system.service.redisservice.RefreshTokenRedisService;
import com.sh.sh.pos.system.service.serviceimpl.customeruserimpl.CustomUserImpl;
import com.sh.sh.pos.system.service.serviceimpl.authserviceimpl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — login, refresh, logout, password reset")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private CustomUserImpl customUserImpl;
    @Mock private PasswordResetRedisService passwordResetRedisService;
    @Mock private EmailService emailService;
    @Mock private EmailTemplateBuilder templateBuilder;
    @Mock private RefreshTokenRedisService refreshTokenRedisService;
    @Mock private JwtBlackListService jwtBlackListService;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("cashier@shpos.com");
        user.setPassword("$2a$10$hashedpassword");

        ReflectionTestUtils.setField(authService, "resetPasswordUrl",
                "http://localhost:5173/reset-password");
    }

    private void stubUserDetailsWithPassword() {
        when(customUserImpl.loadUserByUsername("cashier@shpos.com")).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn("$2a$10$hashedpassword");
    }

    private void stubAuthorities() {
        when(userDetails.getAuthorities())
                .thenAnswer(inv -> List.of(new SimpleGrantedAuthority("ROLE_BRANCH_CASHIER")));
    }

    // ── login ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login → valid credentials → tokens issued, session saved ✅")
    void login_success() {
        stubUserDetailsWithPassword();
        stubAuthorities();
        when(passwordEncoder.matches("correct-pw", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(userRepository.findByEmail("cashier@shpos.com")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        LoginDto dto = new LoginDto();
        dto.setEmail("cashier@shpos.com");
        dto.setPassword("correct-pw");
        dto.setDeviceId("WEB");

        AuthResponse res = authService.login(dto);

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRedisService).save(eq("cashier@shpos.com"), eq("WEB"),
                eq("refresh-token"), anyLong());
        assertThat(user.getLastLogin()).isNotNull();
    }

    @Test
    @DisplayName("login → wrong password → generic 'Invalid email or password' ✅")
    void login_wrongPassword_genericMessage() {
        stubUserDetailsWithPassword();
        when(passwordEncoder.matches("wrong-pw", "$2a$10$hashedpassword")).thenReturn(false);

        LoginDto dto = new LoginDto();
        dto.setEmail("cashier@shpos.com");
        dto.setPassword("wrong-pw");

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(UserException.class)
                .hasMessage("Invalid email or password");

        verify(refreshTokenRedisService, never()).save(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("login → unknown email → SAME generic message (no enumeration) ✅")
    void login_unknownEmail_sameGenericMessage() {
        when(customUserImpl.loadUserByUsername("nobody@shpos.com")).thenReturn(null);

        LoginDto dto = new LoginDto();
        dto.setEmail("nobody@shpos.com");
        dto.setPassword("whatever");

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(UserException.class)
                .hasMessage("Invalid email or password");
    }

    // ── refreshToken ──────────────────────────────────────────────────────

    @Test
    @DisplayName("refreshToken → valid session → tokens rotated ✅")
    void refreshToken_rotates() {
        when(jwtProvider.getTokenType("old-refresh")).thenReturn("REFRESH");
        when(jwtProvider.getEmailFromToken("old-refresh")).thenReturn("cashier@shpos.com");
        when(userRepository.findByEmail("cashier@shpos.com")).thenReturn(user);
        when(refreshTokenRedisService.isValid("cashier@shpos.com", "WEB", "old-refresh")).thenReturn(true);
        when(customUserImpl.loadUserByUsername("cashier@shpos.com")).thenReturn(userDetails);
        stubAuthorities();
        when(jwtProvider.generateAccessToken(any())).thenReturn("new-access");
        when(jwtProvider.generateRefreshToken(any())).thenReturn("new-refresh");

        AuthResponse res = authService.refreshToken("old-refresh", "WEB");

        assertThat(res.getAccessToken()).isEqualTo("new-access");
        assertThat(res.getRefreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenRedisService).save(eq("cashier@shpos.com"), eq("WEB"),
                eq("new-refresh"), anyLong());
    }

    @Test
    @DisplayName("refreshToken → ACCESS token supplied → rejected ✅")
    void refreshToken_wrongTokenType_rejected() {
        when(jwtProvider.getTokenType("an-access-token")).thenReturn("ACCESS");

        assertThatThrownBy(() -> authService.refreshToken("an-access-token", "WEB"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("refreshToken → session missing in Redis → rejected ✅")
    void refreshToken_expiredSession_rejected() {
        when(jwtProvider.getTokenType("old-refresh")).thenReturn("REFRESH");
        when(jwtProvider.getEmailFromToken("old-refresh")).thenReturn("cashier@shpos.com");
        when(userRepository.findByEmail("cashier@shpos.com")).thenReturn(user);
        when(refreshTokenRedisService.isValid("cashier@shpos.com", "WEB", "old-refresh")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("old-refresh", "WEB"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Session expired");
    }

    // ── logout ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout → blacklists access token and deletes session ✅")
    void logout_blacklistsAndDeletesSession() {
        when(jwtProvider.validateToken("raw-token")).thenReturn(true);
        when(jwtProvider.getTokenType("raw-token")).thenReturn("ACCESS");
        when(jwtProvider.getRemainingExpiration("raw-token")).thenReturn(60_000L);

        authService.logout("Bearer raw-token", "cashier@shpos.com", "WEB");

        verify(jwtBlackListService).blacklistToken("raw-token", 60_000L);
        verify(refreshTokenRedisService).delete("cashier@shpos.com", "WEB");
    }

    @Test
    @DisplayName("logout → refresh token supplied → rejected, nothing blacklisted ✅")
    void logout_refreshToken_rejected() {
        when(jwtProvider.validateToken("refresh-tok")).thenReturn(true);
        when(jwtProvider.getTokenType("refresh-tok")).thenReturn("REFRESH");

        assertThatThrownBy(() -> authService.logout("refresh-tok", "cashier@shpos.com", "WEB"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("access token");

        verify(jwtBlackListService, never()).blacklistToken(anyString(), anyLong());
    }

    // ── password reset ────────────────────────────────────────────────────

    @Test
    @DisplayName("createPasswordResetToken → known email → token saved, email sent ✅")
    void createResetToken_knownEmail() throws Exception {
        when(userRepository.findByEmail("cashier@shpos.com")).thenReturn(user);
        when(templateBuilder.buildResetPasswordEmail(anyString())).thenReturn("<html>reset</html>");

        authService.createPasswordResetToken("cashier@shpos.com");

        verify(passwordResetRedisService).saveToken(anyString(), eq("cashier@shpos.com"));
        verify(emailService).sendEmail(eq("cashier@shpos.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("createPasswordResetToken → unknown email → silent, NO email (no enumeration) ✅")
    void createResetToken_unknownEmail_silent() throws Exception {
        when(userRepository.findByEmail("nobody@shpos.com")).thenReturn(null);

        authService.createPasswordResetToken("nobody@shpos.com");

        verify(passwordResetRedisService, never()).saveToken(anyString(), anyString());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("resetPassword → valid token → password encoded, token deleted ✅")
    void resetPassword_success() {
        when(passwordResetRedisService.getEmail("tok-123")).thenReturn("cashier@shpos.com");
        when(userRepository.findByEmail("cashier@shpos.com")).thenReturn(user);
        when(passwordEncoder.encode("new-pw")).thenReturn("$2a$10$newhash");

        authService.resetPassword("tok-123", "new-pw");

        assertThat(user.getPassword()).isEqualTo("$2a$10$newhash");
        verify(userRepository).save(user);
        verify(passwordResetRedisService).deleteToken("tok-123");
    }

    @Test
    @DisplayName("resetPassword → expired/invalid token → rejected ✅")
    void resetPassword_invalidToken_rejected() {
        when(passwordResetRedisService.getEmail("bad-token")).thenReturn(null);

        assertThatThrownBy(() -> authService.resetPassword("bad-token", "new-pw"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Invalid or expired token");

        verify(userRepository, never()).save(any());
    }
}