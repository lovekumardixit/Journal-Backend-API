package com.love.Backend.service;

import com.love.Backend.entity.User;
import com.love.Backend.exception.UserNotFoundException;
import com.love.Backend.repository.UserEntryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserEntryRepo userEntryRepo;

    @Override
    public UserDetails loadUserByUsername(String username){

        User user = userEntryRepo.findByUserName(username);
        if(user == null){
            throw new UserNotFoundException("User not found with this username");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList()
        );
    }
}
