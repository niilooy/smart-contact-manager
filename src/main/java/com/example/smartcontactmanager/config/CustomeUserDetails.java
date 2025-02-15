package com.example.smartcontactmanager.config;

import com.example.smartcontactmanager.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomeUserDetails implements UserDetails {

    private User user;

    public CustomeUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // we are setting authority of user based on its role
        // basically if user is normal user than it will only normal user rights but if it is admin then it will get admin rights
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(user.getRole());
        return List.of(simpleGrantedAuthority);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
