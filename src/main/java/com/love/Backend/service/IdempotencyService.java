package com.love.Backend.service;


import com.love.Backend.repository.UserEntryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    @Autowired
    private UserEntryRepo entryRepo;

    public boolean isDuplicate(String userName){
        return entryRepo.existsByUserName(userName);
    }
}
