package com.sh.sh.pos.system.service.serviceImpl;

import java.time.LocalDateTime;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.configuration.JwtProvider;
import com.sh.sh.pos.system.configuration.EmailTemplate.EmailTemplateBuilder;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.UserMapper;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.response.AuthResponse;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.AuthService;
import com.sh.sh.pos.system.service.EmailService;
import com.sh.sh.pos.system.service.PasswordResetRedisService;
import com.sh.sh.pos.system.service.RedisService.JwtBlackListService;
import com.sh.sh.pos.system.service.RedisService.RefreshTokenRedisService;

import jakarta.mail.MessagingException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final CustomUserImpl customUserImpl;
	private final PasswordResetRedisService passwordResetRedisService;
	private final EmailService emailService;
	private final EmailTemplateBuilder templateBuilder;
	private final RefreshTokenRedisService refreshTokenRedisService;
	private final JwtBlackListService jwtBlackListService;

	@Override
	public AuthResponse register(UserDTO userDTO) throws UserException {
		User existing = userRepository.findByEmail(userDTO.getEmail());

		if (existing != null) {
			throw new UserException("Email already registered!");
		}

		if (userDTO.getRole() == UserRole.ROLE_ADMIN) {
			throw new UserException("Role Admin not allowed!");
		}

		User newUser = new User();
		newUser.setEmail(userDTO.getEmail());
		newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		newUser.setRole(userDTO.getRole());
		newUser.setFullName(userDTO.getFullName());
		newUser.setPhone(userDTO.getPhone());
		newUser.setLastLogin(LocalDateTime.now());

		User savedUser = userRepository.save(newUser);

		// load real authorities
		UserDetails userDetails = customUserImpl.loadUserByUsername(savedUser.getEmail());

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				savedUser.getEmail(),
				null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String accessToken = jwtProvider.generateAccessToken(authentication);
		String refreshToken = jwtProvider.generateRefreshToken(authentication);

		// Redis session save
		String deviceId = "WEB";

		refreshTokenRedisService.save(
				savedUser.getEmail(),
				deviceId,
				refreshToken,
				
				7L * 24 * 60 * 60 * 1000);

		AuthResponse authResponse = new AuthResponse();
		authResponse.setTitle("Welcome: " + newUser.getEmail());
		authResponse.setAccessToken(accessToken);
		authResponse.setRefreshToken(refreshToken);
		authResponse.setMessage("Registered Successfully");
		authResponse.setUser(UserMapper.toDTO(savedUser));
		return authResponse;
	}

	@Override
	public AuthResponse login(LoginDto dto) throws UserException {
		String email = dto.getEmail();
		String password = dto.getPassword();
		String deviceId = dto.getDeviceId() != null ? dto.getDeviceId() : "WEB";

		Authentication authentication = authenticate(email, password);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		String role = authorities.iterator().next().getAuthority();

		String accessToken = jwtProvider.generateAccessToken(authentication);

		String refreshToken = jwtProvider.generateRefreshToken(authentication);
		UserDetails userDetails = customUserImpl.loadUserByUsername(email);
		Authentication fullAuth = new UsernamePasswordAuthenticationToken(
				email,
				null,
				userDetails.getAuthorities());

		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserException("User not found");
		}

		// 5. SAVE SESSION IN REDIS
		refreshTokenRedisService.save(
				email,
				deviceId,
				refreshToken,
				7L * 24 * 60 * 60 * 1000);

		user.setLastLogin(LocalDateTime.now());
		userRepository.save(user);

		AuthResponse authResponse = new AuthResponse();
		authResponse.setAccessToken(accessToken);
		authResponse.setRefreshToken(refreshToken);
		authResponse.setMessage("Welcome: " + email);
		authResponse.setUser(UserMapper.toDTO(user));
		authResponse.setTitle("Login success");

		return authResponse;
	}

	private Authentication authenticate(String email, String password) throws UserException {
		UserDetails userDetails = customUserImpl.loadUserByUsername(email);

		if (userDetails == null) {
			throw new UserException("email doesn't exist" + email);
		}

		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new UserException("password doesn't match");
		}
		return new UsernamePasswordAuthenticationToken(email, null, userDetails.getAuthorities());
	}

	@Override
	public void createPasswordResetToken(String email) throws UserException, MessagingException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			return;
		}
		String token = UUID.randomUUID().toString();

		passwordResetRedisService.saveToken(token, user.getEmail());

		String link = "http://localhost:5173/reset-password?token=" + token;

		String html = templateBuilder.buildResetPasswordEmail(link);

		emailService.sendEmail(
				user.getEmail(),
				"Reset Password - SH POS",
				html);

		System.out.println("=================================");
		System.out.println("RESET TOKEN CREATED");
		System.out.println("EMAIL : " + user.getEmail());
		System.out.println("TOKEN : " + token);
		System.out.println("LINK  : " + link);
		System.out.println("=================================");

	}

	@Override
	public void resetPassword(String token, String newPassword) throws UserException {
		String email = passwordResetRedisService.getEmail(token);

		if (email == null) {
			throw new UserException("Invalid or expired token");
		}

		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UserException("User not found");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		// delete token after use (VERY IMPORTANT)
		passwordResetRedisService.deleteToken(token);
		System.out.println("=================================");
		System.out.println("PASSWORD RESET SUCCESS");
		System.out.println("EMAIL : " + email);
		System.out.println("TOKEN REMOVED FROM REDIS");
		System.out.println("=================================");

	}

	@Override
	public AuthResponse refreshToken(
			String refreshToken,
			String deviceId) throws UserException {

		String type = jwtProvider.getTokenType(refreshToken);

		if (!"REFRESH".equals(type)) {
			throw new UserException("Invalid refresh token");
		}

		String email = jwtProvider.getEmailFromToken(refreshToken);

		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UserException("User not found");
		}

		// check Redis session
		boolean valid = refreshTokenRedisService.isValid(
				email,
				deviceId,
				refreshToken);

		if (!valid) {
			throw new UserException("Session expired or logged out");
		}

		UserDetails userDetails = customUserImpl.loadUserByUsername(email);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				email,
				null,
				userDetails.getAuthorities());

		// ROTATE refresh token
		String newAccessToken = jwtProvider.generateAccessToken(authentication);

		String newRefreshToken = jwtProvider.generateRefreshToken(authentication);

		refreshTokenRedisService.save(
				email,
				deviceId,
				newRefreshToken,
				7L * 24 * 60 * 60 * 1000);

		AuthResponse response = new AuthResponse();
		response.setAccessToken(newAccessToken);
		response.setRefreshToken(newRefreshToken);
		response.setMessage("Token refreshed successfully");
		response.setUser(UserMapper.toDTO(user));

		return response;
	}

	@Override
	public void logout(String token, String email, String deviceId) throws UserException {

		// remove Bearer prefix
		if (token.startsWith("Bearer ")) {
			token = token.substring(7);
		}

		// validate token
		if (!jwtProvider.validateToken(token)) {
			throw new UserException("Invalid token");
		}

		String type = jwtProvider.getTokenType(token);

		if (!"ACCESS".equals(type)) {
			throw new UserException("Logout requires access token");
		}

		// get remaining expiration time
		long remainingTime = jwtProvider.getRemainingExpiration(token);

		// add to blacklist
		jwtBlackListService.blacklistToken(token, remainingTime);

		// OPTIONAL:
		// remove refresh token session from redis if you want
		refreshTokenRedisService.delete(email, deviceId);

		System.out.println("=================================");
		System.out.println("USER LOGGED OUT");
		System.out.println("TOKEN BLACKLISTED");
		System.out.println("EMAIL : " + email);
		System.out.println("DEVICE: " + deviceId);
		System.out.println("=================================");
	};
}
