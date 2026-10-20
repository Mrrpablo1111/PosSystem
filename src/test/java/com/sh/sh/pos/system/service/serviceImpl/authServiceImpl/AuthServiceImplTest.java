package com.sh.sh.pos.system.service.serviceimpl.authServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.GrantedAuthority;
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
import com.sh.sh.pos.system.service.authservice.SmsService;
import com.sh.sh.pos.system.service.emailservice.EmailService;
import com.sh.sh.pos.system.service.redisservice.JwtBlackListService;
import com.sh.sh.pos.system.service.redisservice.PasswordResetRedisService;
import com.sh.sh.pos.system.service.redisservice.RefreshTokenRedisService;
import com.sh.sh.pos.system.service.serviceimpl.authserviceimpl.AuthServiceImpl;
import com.sh.sh.pos.system.service.serviceimpl.customeruserimpl.CustomUserImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Authentication, OTP, Refresh Token and Logout")
class AuthServiceImplTest {

    // =========================================================
    // MOCKS
    // =========================================================

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomUserImpl customUserImpl;

    @Mock
    private PasswordResetRedisService passwordResetRedisService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateBuilder templateBuilder;

    @Mock
    private SmsService smsService;

    @Mock
    private RefreshTokenRedisService refreshTokenRedisService;

    @Mock
    private JwtBlackListService jwtBlackListService;

    @Mock
    private UserDetails userDetails;

    // =========================================================
    // SERVICE
    // =========================================================

    @InjectMocks
    private AuthServiceImpl authService;

    // =========================================================
    // TEST DATA
    // =========================================================

    private User user;

    private static final String EMAIL = "cashier@shpos.com";

    private static final String PHONE = "+85510878487";

    private static final String NORMALIZED_PHONE = "85510878487";

    private static final String PASSWORD = "correct-pw";

    private static final String WRONG_PASSWORD = "wrong-pw";

    private static final String HASHED_PASSWORD =
            "$2a$10$hashedpassword";

    private static final String DEVICE_ID = "WEB";

    private static final String ACCESS_TOKEN =
            "access-token";

    private static final String REFRESH_TOKEN =
            "refresh-token";

    private static final String NEW_ACCESS_TOKEN =
            "new-access";

    private static final String NEW_REFRESH_TOKEN =
            "new-refresh";

    private static final String OTP =
            "370199";

    private static final String RESET_TOKEN =
            "reset-token-123";

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        user = new User();

        user.setId(1L);
        user.setFullName("Test Cashier");
        user.setEmail(EMAIL);
        user.setPhone(PHONE);
        user.setPassword(HASHED_PASSWORD);

        ReflectionTestUtils.setField(
                authService,
                "resetPasswordUrl",
                "http://localhost:5173/reset-password"
        );
    }

    // =========================================================
    // COMMON STUBS
    // =========================================================

    private void stubUserDetailsByEmail() {

        when(customUserImpl.loadUserByUsername(EMAIL))
                .thenReturn(userDetails);

        when(userDetails.getPassword())
                .thenReturn(HASHED_PASSWORD);

        doReturn(
                List.<GrantedAuthority>of(
                        new SimpleGrantedAuthority(
                                "ROLE_BRANCH_CASHIER"
                        )
                )
        ).when(userDetails).getAuthorities();
    }

    private void stubUserDetailsByPhone() {

        when(customUserImpl.loadUserByUsername(PHONE))
                .thenReturn(userDetails);

        when(userDetails.getPassword())
                .thenReturn(HASHED_PASSWORD);

        doReturn(
                List.<GrantedAuthority>of(
                        new SimpleGrantedAuthority(
                                "ROLE_BRANCH_CASHIER"
                        )
                )
        ).when(userDetails).getAuthorities();
    }

    private void stubExistingUser() {

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));
    }

    private void stubExistingUserByPhone() {

        when(userRepository.findByPhone(NORMALIZED_PHONE))
                .thenReturn(Optional.of(user));
    }

    // =========================================================
    // LOGIN - EMAIL
    // =========================================================

    @Test
    @DisplayName("login -> email + valid password -> tokens issued")
    void login_email_success() {

        stubUserDetailsByEmail();
        stubExistingUser();

        when(passwordEncoder.matches(
                PASSWORD,
                HASHED_PASSWORD
        )).thenReturn(true);

        when(jwtProvider.generateAccessToken(any()))
                .thenReturn(ACCESS_TOKEN);

        when(jwtProvider.generateRefreshToken(any()))
                .thenReturn(REFRESH_TOKEN);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        LoginDto dto = new LoginDto();

        dto.setIdentifier(EMAIL);
        dto.setPassword(PASSWORD);
        dto.setDeviceId(DEVICE_ID);

        AuthResponse response =
                authService.login(dto);

        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo(ACCESS_TOKEN);

        assertThat(response.getRefreshToken())
                .isEqualTo(REFRESH_TOKEN);

        assertThat(user.getLastLogin())
                .isNotNull();

        verify(refreshTokenRedisService)
                .save(
                        eq(EMAIL),
                        eq(DEVICE_ID),
                        eq(REFRESH_TOKEN),
                        anyLong()
                );

        verify(jwtProvider)
                .generateAccessToken(any());

        verify(jwtProvider)
                .generateRefreshToken(any());
    }

    // =========================================================
    // LOGIN - PHONE
    // =========================================================

    @Test
    @DisplayName("login -> phone + valid password -> tokens issued")
    void login_phone_success() {

        stubUserDetailsByPhone();
        stubExistingUserByPhone();

        when(passwordEncoder.matches(
                PASSWORD,
                HASHED_PASSWORD
        )).thenReturn(true);

        when(jwtProvider.generateAccessToken(any()))
                .thenReturn(ACCESS_TOKEN);

        when(jwtProvider.generateRefreshToken(any()))
                .thenReturn(REFRESH_TOKEN);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        LoginDto dto = new LoginDto();

        dto.setIdentifier(PHONE);
        dto.setPassword(PASSWORD);
        dto.setDeviceId(DEVICE_ID);

        AuthResponse response =
                authService.login(dto);

        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo(ACCESS_TOKEN);

        assertThat(response.getRefreshToken())
                .isEqualTo(REFRESH_TOKEN);

        verify(jwtProvider)
                .generateAccessToken(any());

        verify(jwtProvider)
                .generateRefreshToken(any());
    }

    // =========================================================
    // LOGIN - WRONG PASSWORD
    // =========================================================

    @Test
    @DisplayName("login -> wrong password -> rejected")
    void login_wrongPassword() {

        stubUserDetailsByEmail();
        stubExistingUser();

        when(passwordEncoder.matches(
                WRONG_PASSWORD,
                HASHED_PASSWORD
        )).thenReturn(false);

        LoginDto dto = new LoginDto();

        dto.setIdentifier(EMAIL);
        dto.setPassword(WRONG_PASSWORD);

        assertThatThrownBy(
                () -> authService.login(dto)
        )
                .isInstanceOf(UserException.class)
                .hasMessage(
                        "Invalid email or password"
                );

        verify(refreshTokenRedisService, never())
                .save(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyLong()
                );

        verify(jwtProvider, never())
                .generateAccessToken(any());

        verify(jwtProvider, never())
                .generateRefreshToken(any());
    }

    // =========================================================
    // LOGIN - UNKNOWN IDENTIFIER
    // =========================================================

    @Test
    @DisplayName("login -> unknown email -> rejected")
    void login_unknownEmail() {

        when(customUserImpl.loadUserByUsername(
                "nobody@shpos.com"
        )).thenReturn(null);

        LoginDto dto = new LoginDto();

        dto.setIdentifier("nobody@shpos.com");
        dto.setPassword(PASSWORD);

        assertThatThrownBy(
                () -> authService.login(dto)
        )
                .isInstanceOf(UserException.class)
                .hasMessage(
                        "Invalid email or password"
                );

        verifyNoInteractions(
                passwordEncoder,
                jwtProvider,
                refreshTokenRedisService
        );
    }

    // =========================================================
    // LOGIN - NULL IDENTIFIER
    // =========================================================

    @Test
    @DisplayName("login -> null identifier -> rejected")
    void login_nullIdentifier() {

        LoginDto dto = new LoginDto();

        dto.setIdentifier(null);
        dto.setPassword(PASSWORD);

        assertThatThrownBy(
                () -> authService.login(dto)
        )
                .isInstanceOf(Exception.class);
    }

    // =========================================================
    // LOGIN - NULL PASSWORD
    // =========================================================

    @Test
    @DisplayName("login -> null password -> rejected")
    void login_nullPassword() {

        LoginDto dto = new LoginDto();

        dto.setIdentifier(EMAIL);
        dto.setPassword(null);

        assertThatThrownBy(
                () -> authService.login(dto)
        )
                .isInstanceOf(Exception.class);
    }

    // =========================================================
    // REFRESH TOKEN - SUCCESS
    // =========================================================

    @Test
    @DisplayName("refresh -> valid refresh token -> tokens rotated")
    void refreshToken_success() {

        stubExistingUser();
        stubUserDetailsByEmail();

        when(jwtProvider.validateRefreshToken(
                REFRESH_TOKEN
        )).thenReturn(true);

        when(jwtProvider.getEmailFromToken(
                REFRESH_TOKEN
        )).thenReturn(EMAIL);

        when(refreshTokenRedisService.isValid(
                EMAIL,
                DEVICE_ID,
                REFRESH_TOKEN
        )).thenReturn(true);

        when(jwtProvider.generateAccessToken(any()))
                .thenReturn(NEW_ACCESS_TOKEN);

        when(jwtProvider.generateRefreshToken(any()))
                .thenReturn(NEW_REFRESH_TOKEN);

        AuthResponse response =
                authService.refreshToken(
                        REFRESH_TOKEN,
                        DEVICE_ID
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo(NEW_ACCESS_TOKEN);

        assertThat(response.getRefreshToken())
                .isEqualTo(NEW_REFRESH_TOKEN);

        verify(refreshTokenRedisService)
                .save(
                        eq(EMAIL),
                        eq(DEVICE_ID),
                        eq(NEW_REFRESH_TOKEN),
                        anyLong()
                );

        verify(refreshTokenRedisService)
                .delete(
                        EMAIL,
                        DEVICE_ID
                );

        verify(jwtProvider)
                .validateRefreshToken(
                        REFRESH_TOKEN
                );
    }

    // =========================================================
    // REFRESH TOKEN - ACCESS TOKEN
    // =========================================================

    @Test
    @DisplayName("refresh -> access token supplied -> rejected")
    void refreshToken_accessTokenRejected() {

        when(jwtProvider.validateRefreshToken(
                ACCESS_TOKEN
        )).thenReturn(false);

        assertThatThrownBy(
                () -> authService.refreshToken(
                        ACCESS_TOKEN,
                        DEVICE_ID
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Invalid refresh token"
                );

        verify(
                refreshTokenRedisService,
                never()
        ).isValid(
                anyString(),
                anyString(),
                anyString()
        );
    }

    // =========================================================
    // REFRESH TOKEN - INVALID
    // =========================================================

    @Test
    @DisplayName("refresh -> invalid token -> rejected")
    void refreshToken_invalidTokenRejected() {

        when(jwtProvider.validateRefreshToken(
                "bad-token"
        )).thenReturn(false);

        assertThatThrownBy(
                () -> authService.refreshToken(
                        "bad-token",
                        DEVICE_ID
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Invalid refresh token"
                );

        verify(jwtProvider)
                .validateRefreshToken(
                        "bad-token"
                );

        verifyNoInteractions(
                refreshTokenRedisService
        );
    }

    // =========================================================
    // REFRESH TOKEN - REDIS SESSION EXPIRED
    // =========================================================

    @Test
    @DisplayName("refresh -> Redis session missing -> rejected")
    void refreshToken_sessionExpired() {

        stubExistingUser();

        when(jwtProvider.validateRefreshToken(
                REFRESH_TOKEN
        )).thenReturn(true);

        when(jwtProvider.getEmailFromToken(
                REFRESH_TOKEN
        )).thenReturn(EMAIL);

        when(refreshTokenRedisService.isValid(
                EMAIL,
                DEVICE_ID,
                REFRESH_TOKEN
        )).thenReturn(false);

        assertThatThrownBy(
                () -> authService.refreshToken(
                        REFRESH_TOKEN,
                        DEVICE_ID
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Session expired"
                );

        verify(refreshTokenRedisService)
                .isValid(
                        EMAIL,
                        DEVICE_ID,
                        REFRESH_TOKEN
                );

        verify(jwtProvider, never())
                .generateAccessToken(any());

        verify(jwtProvider, never())
                .generateRefreshToken(any());
    }

    // =========================================================
    // LOGOUT - SUCCESS
    // =========================================================

    @Test
    @DisplayName("logout -> access token blacklisted and refresh session deleted")
    void logout_success() {

        when(jwtProvider.validateAccessToken(
                "raw-token"
        )).thenReturn(true);

        when(jwtProvider.getEmailFromToken(
                "raw-token"
        )).thenReturn(EMAIL);

        when(jwtProvider.getRemainingExpiration(
                "raw-token"
        )).thenReturn(60_000L);

        authService.logout(
                "Bearer raw-token",
                EMAIL,
                DEVICE_ID
        );

        verify(jwtBlackListService)
                .blacklistToken(
                        "raw-token",
                        60_000L
                );

        verify(refreshTokenRedisService)
                .delete(
                        EMAIL,
                        DEVICE_ID
                );

        verify(jwtProvider)
                .validateAccessToken(
                        "raw-token"
                );
    }

    // =========================================================
    // LOGOUT - REFRESH TOKEN
    // =========================================================

    @Test
    @DisplayName("logout -> refresh token -> rejected")
    void logout_refreshTokenRejected() {

        when(jwtProvider.validateAccessToken(
                REFRESH_TOKEN
        )).thenReturn(false);

        assertThatThrownBy(
                () -> authService.logout(
                        REFRESH_TOKEN,
                        EMAIL,
                        DEVICE_ID
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "access token"
                );

        verify(jwtBlackListService, never())
                .blacklistToken(
                        anyString(),
                        anyLong()
                );

        verify(refreshTokenRedisService, never())
                .delete(
                        anyString(),
                        anyString()
                );
    }

    // =========================================================
    // LOGOUT - INVALID ACCESS TOKEN
    // =========================================================

    @Test
    @DisplayName("logout -> invalid access token -> rejected")
    void logout_invalidTokenRejected() {

        when(jwtProvider.validateAccessToken(
                "bad-token"
        )).thenReturn(false);

        assertThatThrownBy(
                () -> authService.logout(
                        "bad-token",
                        EMAIL,
                        DEVICE_ID
                )
        )
                .isInstanceOf(UserException.class);

        verify(jwtBlackListService, never())
                .blacklistToken(
                        anyString(),
                        anyLong()
                );
    }

    // =========================================================
    // LOGOUT - EMAIL MISMATCH
    // =========================================================

    @Test
    @DisplayName("logout -> request email differs from token email -> rejected")
    void logout_emailMismatchRejected() {

        when(jwtProvider.validateAccessToken(
                "raw-token"
        )).thenReturn(true);

        when(jwtProvider.getEmailFromToken(
                "raw-token"
        )).thenReturn(EMAIL);

        assertThatThrownBy(
                () -> authService.logout(
                        "raw-token",
                        "another@shpos.com",
                        DEVICE_ID
                )
        )
                .isInstanceOf(UserException.class);

        verify(jwtBlackListService, never())
                .blacklistToken(
                        anyString(),
                        anyLong()
                );

        verify(refreshTokenRedisService, never())
                .delete(
                        anyString(),
                        anyString()
                );
    }

    // =========================================================
    // OTP - EMAIL
    // =========================================================

    @Test
    @DisplayName("forgot password -> email -> OTP generated and sent")
    void createPasswordResetOtp_email_success()
            throws Exception {

        stubExistingUser();

        when(passwordResetRedisService.getOtp(
                EMAIL
        )).thenReturn(null);

        when(templateBuilder.buildPasswordResetOtpEmail(
                eq(user.getFullName()),
                anyString()
        )).thenReturn(
                "<html>OTP</html>"
        );

        authService.createPasswordResetOtp(
                EMAIL
        );

        verify(passwordResetRedisService)
                .saveOtp(
                        eq(EMAIL),
                        anyString()
                );

        verify(emailService)
                .sendEmail(
                        eq(EMAIL),
                        anyString(),
                        eq("<html>OTP</html>")
                );

        verifyNoInteractions(
                smsService
        );
    }

    // =========================================================
    // OTP - PHONE / SMS
    // =========================================================

    @Test
    @DisplayName("forgot password -> phone -> OTP generated and sent by SMS")
    void createPasswordResetOtp_phone_success()
            throws Exception {

        when(userRepository.findByPhone(
                NORMALIZED_PHONE
        )).thenReturn(
                Optional.of(user)
        );

        authService.createPasswordResetOtp(
                PHONE
        );

        verify(passwordResetRedisService)
                .saveOtp(
                        eq(PHONE),
                        anyString()
                );

        verify(smsService)
                .sendOtp(
                        eq(PHONE),
                        anyString()
                );

        verifyNoInteractions(
                emailService
        );
    }

    // =========================================================
    // OTP - UNKNOWN EMAIL
    // =========================================================

    @Test
    @DisplayName("forgot password -> unknown email -> no OTP sent")
    void createPasswordResetOtp_unknownEmail()
            throws Exception {

        String unknownEmail =
                "nobody@shpos.com";

        when(userRepository.findByEmail(
                unknownEmail
        )).thenReturn(
                Optional.empty()
        );

        authService.createPasswordResetOtp(
                unknownEmail
        );

        verify(
                passwordResetRedisService,
                never()
        ).saveOtp(
                anyString(),
                anyString()
        );

        verifyNoInteractions(
                emailService,
                smsService
        );
    }

    // =========================================================
    // OTP - UNKNOWN PHONE
    // =========================================================

    @Test
    @DisplayName("forgot password -> unknown phone -> no OTP sent")
    void createPasswordResetOtp_unknownPhone()
            throws Exception {

        String unknownPhone =
                "+85512345678";

        when(userRepository.findByPhone(
                "85512345678"
        )).thenReturn(
                Optional.empty()
        );

        authService.createPasswordResetOtp(
                unknownPhone
        );

        verify(
                passwordResetRedisService,
                never()
        ).saveOtp(
                anyString(),
                anyString()
        );

        verifyNoInteractions(
                emailService,
                smsService
        );
    }

    // =========================================================
    // VERIFY OTP - SUCCESS
    // =========================================================

    @Test
    @DisplayName("verify OTP -> valid OTP -> reset token generated")
    void verifyPasswordResetOtp_success() {

        when(passwordResetRedisService.hasExceededOtpAttempts(
                EMAIL
        )).thenReturn(false);

        when(passwordResetRedisService.getOtp(
                EMAIL
        )).thenReturn(OTP);

        String resetToken =
                authService.verifyPasswordResetOtp(
                        EMAIL,
                        OTP
                );

        assertThat(resetToken)
                .isNotBlank();

        verify(passwordResetRedisService)
                .saveResetToken(
                        eq(resetToken),
                        eq(EMAIL)
                );

        verify(passwordResetRedisService)
                .deleteOtp(
                        EMAIL
                );
    }

    // =========================================================
    // VERIFY OTP - WRONG OTP
    // =========================================================

    @Test
    @DisplayName("verify OTP -> wrong OTP -> rejected")
    void verifyPasswordResetOtp_wrongOtp() {

        when(passwordResetRedisService.hasExceededOtpAttempts(
                EMAIL
        )).thenReturn(false);

        when(passwordResetRedisService.getOtp(
                EMAIL
        )).thenReturn(OTP);

        assertThatThrownBy(
                () -> authService.verifyPasswordResetOtp(
                        EMAIL,
                        "111111"
                )
        )
                .isInstanceOf(UserException.class);

        verify(passwordResetRedisService, never())
                .saveResetToken(
                        anyString(),
                        anyString()
                );

        verify(passwordResetRedisService, never())
                .deleteOtp(
                        anyString()
                );
    }

    // =========================================================
    // VERIFY OTP - OTP NOT FOUND / EXPIRED
    // =========================================================

    @Test
    @DisplayName("verify OTP -> OTP expired or missing -> rejected")
    void verifyPasswordResetOtp_expired() {

        when(passwordResetRedisService.hasExceededOtpAttempts(
                EMAIL
        )).thenReturn(false);

        when(passwordResetRedisService.getOtp(
                EMAIL
        )).thenReturn(null);

        assertThatThrownBy(
                () -> authService.verifyPasswordResetOtp(
                        EMAIL,
                        OTP
                )
        )
                .isInstanceOf(UserException.class);

        verify(passwordResetRedisService, never())
                .saveResetToken(
                        anyString(),
                        anyString()
                );
    }

    // =========================================================
    // VERIFY OTP - TOO MANY ATTEMPTS
    // =========================================================

    @Test
    @DisplayName("verify OTP -> too many attempts -> rejected")
    void verifyPasswordResetOtp_tooManyAttempts() {

        when(passwordResetRedisService.hasExceededOtpAttempts(
                EMAIL
        )).thenReturn(true);

        assertThatThrownBy(
                () -> authService.verifyPasswordResetOtp(
                        EMAIL,
                        OTP
                )
        )
                .isInstanceOf(UserException.class);

        verify(passwordResetRedisService, never())
                .getOtp(
                        anyString()
                );

        verify(passwordResetRedisService, never())
                .saveResetToken(
                        anyString(),
                        anyString()
                );
    }

    // =========================================================
    // RESET PASSWORD - SUCCESS
    // =========================================================

    @Test
    @DisplayName("reset password -> valid reset token -> password changed")
    void resetPassword_success() {

        when(passwordResetRedisService.getEmailFromResetToken(
                RESET_TOKEN
        )).thenReturn(EMAIL);

        stubExistingUser();

        when(passwordEncoder.encode(
                "new-pw"
        )).thenReturn(
                "$2a$10$newhash"
        );

        authService.resetPassword(
                RESET_TOKEN,
                "new-pw"
        );

        assertThat(user.getPassword())
                .isEqualTo(
                        "$2a$10$newhash"
                );

        verify(userRepository)
                .save(user);

        verify(passwordResetRedisService)
                .deleteResetToken(
                        RESET_TOKEN
                );
    }

    // =========================================================
    // RESET PASSWORD - INVALID TOKEN
    // =========================================================

    @Test
    @DisplayName("reset password -> invalid token -> rejected")
    void resetPassword_invalidToken() {

        when(passwordResetRedisService.getEmailFromResetToken(
                "bad-token"
        )).thenReturn(null);

        assertThatThrownBy(
                () -> authService.resetPassword(
                        "bad-token",
                        "new-pw"
                )
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining(
                        "Invalid or expired token"
                );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(passwordResetRedisService, never())
                .deleteResetToken(
                        anyString()
                );
    }

    // =========================================================
    // RESET PASSWORD - USER NOT FOUND
    // =========================================================

    @Test
    @DisplayName("reset password -> token valid but user missing -> rejected")
    void resetPassword_userNotFound() {

        when(passwordResetRedisService.getEmailFromResetToken(
                RESET_TOKEN
        )).thenReturn(EMAIL);

        when(userRepository.findByEmail(
                EMAIL
        )).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () -> authService.resetPassword(
                        RESET_TOKEN,
                        "new-pw"
                )
        )
                .isInstanceOf(Exception.class);

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(passwordResetRedisService, never())
                .deleteResetToken(
                        RESET_TOKEN
                );
    }

    // =========================================================
    // RESET PASSWORD - EMPTY PASSWORD
    // =========================================================

    @Test
    @DisplayName("reset password -> empty password -> rejected")
    void resetPassword_emptyPassword() {

        assertThatThrownBy(
                () -> authService.resetPassword(
                        RESET_TOKEN,
                        ""
                )
        )
                .isInstanceOf(Exception.class);

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }
}
