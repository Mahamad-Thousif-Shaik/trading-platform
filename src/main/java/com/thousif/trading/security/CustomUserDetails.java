package com.thousif.trading.security;

import com.thousif.trading.entity.User;
import com.thousif.trading.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private List<GrantedAuthority> roles;
    private boolean isAccountLocked;
    private boolean isEnabled;

    public CustomUserDetails(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.roles = user.getRoles().stream().map(
                role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        this.isAccountLocked = user.getStatus() != UserStatus.ACTIVE;
        this.isEnabled = user.getStatus() == UserStatus.ACTIVE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
