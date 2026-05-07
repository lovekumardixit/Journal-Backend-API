package com.love.Backend.service;

import com.love.Backend.entity.User;
import com.love.Backend.repository.UserEntryRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTests {

    @Mock
    private UserEntryRepo userRepo;

    @InjectMocks
    private UserDetailsServiceImpl detailsService;

    @Test
    void loadUserByUserNameTest(){

        when(userRepo.findByUserName(anyString()))
                .thenReturn(
                        User.builder()
                                .userName("Vikas")
                                .password("Vikas")
                                .build()
                );

        UserDetails user = detailsService.loadUserByUsername("Kajal");

        assertEquals("Vikas", user.getUsername());
    }
}