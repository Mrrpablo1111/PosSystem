package com.sh.sh.pos.system.service.serviceImpl;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.configuration.JwtProvider;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exceptions.UserException;

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.request.UpdateUserDto;

import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;

	@Override
	public User getUserFromJwtToken(String jwt) throws UserException {
		String email = jwtProvider.getEmailFromToken(jwt);
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UserException("Invaild token");
		}
		return user;
	}

	@Override
	public User getCurrentUser() throws UserException {
		Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

		System.out.println("Authentication = " + authentication);

		if (authentication != null) {
			System.out.println("Name = " + authentication.getName());
			System.out.println("Authorities = " + authentication.getAuthorities());
		}
		String email = authentication.getName();
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserException("user not found");
		}
		return user;
	}

	@Override
	public User getUserByEmail(String email) throws UserException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserException("user not found");
		}
		return user;
	}

	@Override
	public User getUserById(Long id) throws UserException {
		return userRepository.findById(id).orElse(null);
	}

	@Override
	public List<User> getAllUser() throws UserException {

		return userRepository.findAll();
	}

	@Override
	public Set<User> getUserByRole(UserRole role) throws UserException {
		return userRepository.findByRole(role);
	}

	@Override
	public User updateUser(UpdateUserDto updateData, User user) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
	}

}
