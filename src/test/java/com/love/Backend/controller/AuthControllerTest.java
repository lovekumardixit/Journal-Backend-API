package com.love.Backend.controller;

import com.love.Backend.entity.RefreshToken;
import com.love.Backend.entity.User;
import com.love.Backend.repository.UserEntryRepo;
import com.love.Backend.service.JwtService;
import com.love.Backend.service.RefreshTokenService;
import com.love.Backend.service.TokenBlacklistService;
import com.love.Backend.service.UserEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserEntryRepo userRepo;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    public void testLogin_Success() throws Exception {
        User user = User.builder().userName("testuser").password("password").roles(java.util.List.of("USER")).build();
        when(userRepo.findByUserName("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password", "password")).thenReturn(true);
        when(jwtService.generateToken("testuser", "[USER]")).thenReturn("accessToken");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refreshToken");
        when(refreshTokenService.createRefreshToken("testuser")).thenReturn(refreshToken);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"testuser\",\"password\":\"password\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    public void testLogin_InvalidCredentials() throws Exception {
        when(userRepo.findByUserName("testuser")).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testuser\",\"password\":\"wrong\"}")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetProfile() throws Exception {
        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello testuser"));
    }

    @Test
    public void testRefreshToken() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("refreshToken");
        rt.setUsername("testuser");
        when(refreshTokenService.verifyToken("refreshToken")).thenReturn(rt);
        when(jwtService.generateToken("testuser", "USER")).thenReturn("newAccessToken");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refreshToken\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testLogout() throws Exception {
        when(jwtService.getExpirationTime(anyString())).thenReturn(System.currentTimeMillis() + 10000);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));
    }
}
