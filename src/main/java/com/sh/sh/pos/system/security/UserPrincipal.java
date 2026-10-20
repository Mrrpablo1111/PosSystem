
package com.sh.sh.pos.system.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sh.sh.pos.system.model.user.User;

import lombok.Getter;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<GrantedAuthority> authorities;

    public UserPrincipal(User user) {

        this.id = user.getId();

        this.email = user.getEmail();

        this.password = user.getPassword();

        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority(
                        user.getRole().name()
                )
        );
    }

    /**
     * Spring Security uses this value for:
     *
     * authentication.getName()
     *
     * Our JWT uses user ID as the subject,
     * so this MUST return the user ID.
     */
    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

