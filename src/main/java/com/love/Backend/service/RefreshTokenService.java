package com.love.Backend.service;


import com.love.Backend.entity.RefreshToken;
import com.love.Backend.exception.BadRequestException;
import com.love.Backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository repo;

    public RefreshToken createRefreshToken(String username){
        RefreshToken token = new RefreshToken();
        token.setUsername(username);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusSeconds(7*24*60*60));
        return repo.save(token);
    }

    public RefreshToken verifyToken(String token){
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if(rt.getExpiryDate().isBefore(Instant.now())){
            repo.delete(rt);
            throw new BadRequestException("Refresh token expired");
        }
        return rt;
    }

    public void deleteToken(String token){
        repo.deleteByToken(token);
    }
}
