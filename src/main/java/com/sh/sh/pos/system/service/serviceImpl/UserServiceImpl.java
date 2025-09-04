package com.sh.sh.pos.system.service.serviceImpl;


import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.configuration.JwtProvider;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.repository.UserRepository;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
	
	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;
	
	@Override
	public User getUserFromJwtToken(String jwt) throws UserException {
		String email = jwtProvider.getEmailFromToken(jwt);
		User user = userRepository.findByEmail(email);
		
		if(user == null) {
			throw new UserException("Invaild token");
		}
		return user;
	}

	@Override
	public User getCurrentUser() throws UserException {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email);
		if(user == null) {
			throw new UserException("user not found");
		}
		return user;
	}

	@Override
	public User getUserByEmail(String email) throws UserException {
		User user = userRepository.findByEmail(email);
		if(user == null) {
			throw new UserException("user not found");
		}
		return user;
	}

	@Override
	public User getUserById(Long id) {
		
		return userRepository.findById(id).orElse(null);
	}

	@Override
	public List<User> getAllUser() {
		
		return userRepository.findAll();
	}

}
