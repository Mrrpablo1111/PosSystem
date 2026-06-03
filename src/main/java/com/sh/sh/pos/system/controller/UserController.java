package com.sh.sh.pos.system.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.configuration.JwtProvider;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.UserMapper;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.payload.request.ForgotPasswordDto;
import com.sh.sh.pos.system.payload.request.UpdateUserDto;
import com.sh.sh.pos.system.payload.request.ResetPasswordDto;
import com.sh.sh.pos.system.payload.response.MessageResponse;

import com.sh.sh.pos.system.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/profile")
	public ResponseEntity<UserDTO> getUserProfileFromJwtHandler(@RequestHeader("Authorization") String jwt)
			throws UserException {
		User user = userService.getUserFromJwtToken(jwt);
		UserDTO userDTO = UserMapper.toDTO(user);
		return new ResponseEntity<>(userDTO, HttpStatus.OK);
	}

	@GetMapping("/customer")
	public ResponseEntity<Set<UserDTO>> getCustomerList()
			throws UserException {
		Set<User> users = userService.getUserByRole(UserRole.ROLE_CUSTOMER);
		Set<UserDTO> userDTO = UserMapper.toDTOSet(users);
		return new ResponseEntity<>(userDTO, HttpStatus.OK);
	}

	@GetMapping("/cashier")
	public ResponseEntity<Set<UserDTO>> getCashierList(@RequestHeader("Authorization") String jwt)
			throws UserException {
		Set<User> users = userService.getUserByRole(UserRole.ROLE_BRANCH_CASHIER);
		Set<UserDTO> userDTO = UserMapper.toDTOSet(users);
		return new ResponseEntity<>(userDTO, HttpStatus.OK);
	}

	@GetMapping("/list")
	public ResponseEntity<List<UserDTO>> getUsersListHandler(
			@RequestHeader("Authorization") String jwt) throws UserException {

		List<User> users = userService.getAllUser();

		List<UserDTO> userDTOs = users.stream()
				.map(UserMapper::toDTO)
				.toList();

		return ResponseEntity.ok(userDTOs);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserDTO> getUserByIdHandler(@PathVariable Long userId) throws UserException {
		User user = userService.getUserById(userId);
		UserDTO userDTO = UserMapper.toDTO(user);

		return new ResponseEntity<>(userDTO, HttpStatus.OK);
	}

	@PatchMapping("/update-profile")
	public ResponseEntity<User> updateUserDetailsHanlder(@RequestBody UpdateUserDto updateData,
			@RequestHeader("Authorization") String jwt) throws UserException {
		User user = userService.getUserFromJwtToken(jwt);
		User updatedUser = userService.updateUser(updateData, user);
		return ResponseEntity.ok(updatedUser);
	}

	

}
