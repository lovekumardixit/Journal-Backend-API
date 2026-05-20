package com.love.Backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    public void testGenerateToken() {
        String token = jwtService.generateToken("testuser", "USER");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    public void testExtractUsername() {
        String token = jwtService.generateToken("testuser", "USER");
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    public void testValidateToken() {
        String token = jwtService.generateToken("testuser", "USER");
        boolean isValid = jwtService.validateToken(token, "testuser");
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_InvalidUser() {
        String token = jwtService.generateToken("testuser", "USER");
        boolean isValid = jwtService.validateToken(token, "otheruser");
        assertFalse(isValid);
    }

    @Test
    public void testGetExpirationTime() {
        String token = jwtService.generateToken("testuser", "USER");
        long expiry = jwtService.getExpirationTime(token);
        assertTrue(expiry > System.currentTimeMillis());
    }
}
