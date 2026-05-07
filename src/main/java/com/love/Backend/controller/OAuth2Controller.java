package com.love.Backend.controller;

import com.google.gson.Gson;
import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.dto.response.AuthResponse;
import com.love.Backend.entity.RefreshToken;
import com.love.Backend.entity.User;
import com.love.Backend.exception.BadRequestException;
import com.love.Backend.service.JwtService;
import com.love.Backend.service.RefreshTokenService;
import com.love.Backend.service.UserEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Tag(name = "OAuth2 APIs", description = "Google OAuth2 login endpoint") 
@RestController 
@RequestMapping("/auth/oauth2") 
public class OAuth2Controller {

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"; 
    private static final String GOOGLE_USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo"; 

    @Autowired 
    private UserEntryService userService; 

    @Autowired 
    private JwtService jwtService; 

    @Autowired 
    private RefreshTokenService refreshTokenService; 

    @Value("${app.oauth2.google.client-id:}") 
    private String googleClientId; 

    @Value("${app.oauth2.google.client-secret:}") 
    private String googleClientSecret; 

    @Value("${app.oauth2.google.redirect-uri:http://localhost:8081/auth/oauth2/callback}") 
    private String googleRedirectUri; 

    @Operation(summary = "Get Google Login URL", description = "Returns the Google OAuth2 login URL") 
    @ApiResponse(responseCode = "200", description = "URL returned successfully") 
    @GetMapping("/login") 
    public Map<String, String> initiateLogin() { 
        ensureGoogleOAuthConfigured();
        String googleLoginUrl = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile openid")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("approval_prompt", "force")
                .queryParam("include_granted_scopes", "true")
                .build()
                .toUriString();
        return Map.of("loginUrl", googleLoginUrl, "message", "Open this URL in browser after configuring Google OAuth client in Google Cloud Console."); 
    }

    @Operation(summary = "Handle Google OAuth2 Callback", description = "Exchanges authorization code for app access and refresh tokens") 
    @ApiResponse(responseCode = "200", description = "Login successful, app tokens returned") 
    @GetMapping("/callback") 
    public AuthResponse handleGoogleCallback(@RequestParam String code) { 
        ensureGoogleOAuthConfigured(); 
        String googleAccessToken = exchangeCodeForToken(code); 
        Map<String, Object> userInfo = getUserInfoFromGoogle(googleAccessToken); 
        User user = findOrCreateUser(userInfo); 
        String appAccessToken = jwtService.generateToken(user.getUserName(), user.getRoles().toString()); 
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserName()); 
        return new AuthResponse(appAccessToken, refreshToken.getToken(), "Bearer"); 
    }

    private String exchangeCodeForToken(String authCode) { 
        RestTemplate restTemplate = new RestTemplate(); 
        HttpHeaders headers = new HttpHeaders(); 
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); 
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>(); 
        body.add("code", authCode); 
        body.add("client_id", googleClientId); 
        body.add("client_secret", googleClientSecret); 
        body.add("redirect_uri", googleRedirectUri); 
        body.add("grant_type", "authorization_code"); 
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers); 
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(GOOGLE_TOKEN_URL, request, String.class); 
        Map<?, ?> responseBody = new Gson().fromJson(tokenResponse.getBody(), Map.class); 
        Object accessToken = responseBody.get("access_token"); 
        if (accessToken == null) { 
            throw new BadRequestException("Failed to exchange Google authorization code"); 
        }
        return accessToken.toString(); 
    }

    private Map<String, Object> getUserInfoFromGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_USERINFO_URL,
                org.springframework.http.HttpMethod.GET,
                request,
                String.class
        );

        if (response.getBody() == null) {
            throw new BadRequestException("Failed to fetch Google user info");
        }

        // Debugging ke liye raw response print karo
        System.out.println("Google User Info Raw Response: " + response.getBody());

        Map<String, Object> userInfo = new Gson().fromJson(response.getBody(), Map.class);

        // Email validation
        if (!userInfo.containsKey("email")) {
            throw new BadRequestException("Google did not return email. Check OAuth scopes and consent screen.");
        }

        return userInfo;
    }

    private User findOrCreateUser(Map<String, Object> googleUserInfo) { 
        String email = String.valueOf(googleUserInfo.get("email")); 
        if (email == null || email.isBlank() || "null".equals(email)) { 
            throw new BadRequestException("Google account email is required"); 
        }
        User existingUser = userService.findByName(email); 
        if (existingUser != null) { 
            return existingUser; 
        }
        UserRequestDTO userDTO = new UserRequestDTO(); 
        userDTO.setUserName(email); 
        userDTO.setEmail(email); 
        userDTO.setPassword("OAuth2_" + System.nanoTime()); 
        userDTO.setSentimentAnalysis(true); 
        userService.saveNewUser(userDTO); 
        return userService.findByName(email); 
    }

    private void ensureGoogleOAuthConfigured() { 
        if (googleClientId == null || googleClientId.isBlank() || googleClientSecret == null || googleClientSecret.isBlank()) { 
            throw new BadRequestException("Google OAuth is not configured. Set GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET and GOOGLE_REDIRECT_URI."); 
        }
    }
}
