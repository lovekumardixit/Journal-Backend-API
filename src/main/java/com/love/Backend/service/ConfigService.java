package com.love.Backend.service;

import com.love.Backend.repository.BackendConfigRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ConfigService {

    @Autowired
    private BackendConfigRepo configRepo;

    public String getValue(String key){
        return configRepo.getConfigValue(key);
    }

}
