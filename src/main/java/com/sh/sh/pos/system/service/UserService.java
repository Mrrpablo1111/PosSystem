package com.sh.sh.pos.system.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.request.UpdateUserDto;

import jakarta.mail.MessagingException;


@Service
public interface UserService {
	User getUserFromJwtToken(String jwt) throws UserException;
	User getCurrentUser() throws UserException;
	User getUserByEmail(String email) throws UserException;
	User getUserById(Long id) throws UserException;
	Set<User> getUserByRole(UserRole role) throws UserException;
	List<User> getAllUser()throws UserException;
	User updateUser(UpdateUserDto updateData, User user);
	
	
} 
