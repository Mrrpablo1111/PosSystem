package com.sh.sh.pos.system.service.serviceimpl.customeruserimpl;

import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        if (identifier == null || identifier.isBlank()) {
            throw new UsernameNotFoundException("Email or phone number is required");
        }

        String value = identifier.trim();

        User user;

        if (value.contains("@")) {

            String email = value.toLowerCase();
            user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                    "Invalid email or phone number"));
        } else {
            String phone = normalizePhone(value);
            user = userRepository.findByPhone(phone).orElseThrow(
                    () -> new UsernameNotFoundException("Invalid email or phone number"));
        }

        if (user.getRole() == null) {
            throw new UsernameNotFoundException("User role is not assignt");
        }

        return new UserPrincipal(user);
    }

    private String normalizePhone(String phone) {
        String value = phone.trim();

        value = value.replaceAll("\\s+", "");

        value = value.replace("-", "");

        // Cambodia local format
        if (value.startsWith("0")) {
            value = "+855" + value.substring(1);
        }

        if (value.startsWith("855")) {
            value = "+" + value;
        }
        return value;
    }
}
