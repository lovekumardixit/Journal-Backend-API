package com.love.Backend.controller;

import com.love.Backend.dto.request.LoginRequest;
import com.love.Backend.dto.request.LogoutRequest;
import com.love.Backend.dto.request.RefreshTokenRequest;
import com.love.Backend.dto.response.AuthResponse;
import com.love.Backend.entity.RefreshToken;
import com.love.Backend.entity.User;
import com.love.Backend.exception.InvalidCredentialsException;
import com.love.Backend.repository.UserEntryRepo;
import com.love.Backend.service.JwtService;
import com.love.Backend.service.RefreshTokenService;
import com.love.Backend.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Authentication APIs", description = "Login, refresh token, logout and profile APIs") 
@RestController 
@RequestMapping("/auth") 
public class AuthController {

    @Autowired 
    private JwtService jwtService; 

    @Autowired 
    private UserEntryRepo userRepo; 

    @Autowired 
    private PasswordEncoder passwordEncoder; 

    @Autowired 
    private RefreshTokenService refreshTokenService; 

    @Autowired 
    private TokenBlacklistService tokenBlacklistService; 

    @Operation(summary = "User Login", description = "Authenticate user with username and password, returns access and refresh tokens") 
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Login successful"), @ApiResponse(responseCode = "401", description = "Invalid credentials")}) 
    @PostMapping("/login") 
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { 
        // Normalize username to lowercase for comparison
        String normalizedUsername = request.getUserName().toLowerCase();
        User user = userRepo.findByUserName(normalizedUsername);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password"); 
        }
        String accessToken = jwtService.generateToken(user.getUserName(), user.getRoles().toString()); 
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserName()); 
        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer"); 
    }

    @Operation(summary = "Get User Profile", description = "Returns a greeting message with the authenticated user's name") 
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully") 
    @GetMapping("/profile") 
    public Map<String, String> getProfile() { 
        String username = SecurityContextHolder.getContext().getAuthentication().getName(); 
        return Map.of("message", "Hello " + username); 
    }

    @Operation(summary = "Refresh Access Token", description = "Generate a new access token using a valid refresh token") 
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "New access token generated"), @ApiResponse(responseCode = "400", description = "Invalid refresh token")}) 
    @PostMapping("/refresh") 
    public Map<String, String> refresh(@Valid @RequestBody RefreshTokenRequest request) { 
        RefreshToken rt = refreshTokenService.verifyToken(request.getRefreshToken()); 
        String newAccessToken = jwtService.generateToken(rt.getUsername(), "USER"); 
        return Map.of("accessToken", newAccessToken, "tokenType", "Bearer"); 
    }

    @Operation(summary = "User Logout", description = "Blacklist the access token and delete the refresh token for logout") 
    @ApiResponse(responseCode = "200", description = "Logged out successfully") 
    @PostMapping("/logout") 
    public Map<String, String> logout(@RequestHeader("Authorization") String header, @Valid @RequestBody(required = false) LogoutRequest request) { 
        String accessToken = header.substring(7); 
        long expiry = jwtService.getExpirationTime(accessToken); 
        tokenBlacklistService.blacklistToken(accessToken, expiry); 
        if (request != null && request.getRefreshToken() != null) { 
            refreshTokenService.deleteToken(request.getRefreshToken()); 
        }
        return Map.of("message", "Logged out"); 
    }
}
