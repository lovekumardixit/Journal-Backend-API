package com.love.Backend.repository;

import com.love.Backend.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {


    Optional<RefreshToken> findByToken(String Token);
    void deleteByToken(String Token);
}
