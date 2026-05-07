package com.love.Backend.cache;


import com.love.Backend.entity.BackendConfigEntity;
import com.love.Backend.repository.BackendConfigRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppCache {

    public enum keys{
        ELEVEN_LABS_API;
    }

    @Autowired
    private BackendConfigRepo backendConfigRepo;

    public Map<String, String> appCache;

    @PostConstruct
    public void init(){
         appCache = new HashMap<>(); 
        
        List<BackendConfigEntity> all = backendConfigRepo.findAll();
        for (BackendConfigEntity backendConfigEntity : all) {
            appCache.put(backendConfigEntity.getKey(), backendConfigEntity.getValue());
        }
    }
}
