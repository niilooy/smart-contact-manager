package com.example.smartcontactmanager.config;

import com.example.smartcontactmanager.dao.UserRepository;
import com.example.smartcontactmanager.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // fetching user details from database.
        User user = userRepository.getUserByUserName(username);

        if(user == null){
            throw  new UsernameNotFoundException("Could not found userName please try with different userName");
        }

        CustomeUserDetails customeUserDetails = new CustomeUserDetails(user);

        return customeUserDetails;
    }
    // details
}
