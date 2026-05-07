package com.love.Backend.service;

import com.love.Backend.cache.AppCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class ElevenLabsService {

    @Value("${Eleven.Labs.Api}")
    private String API_KEY;

    @Autowired
    private AppCache appCache;

    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] convertTextToSpeech(String text) {

        
        String finalUrl = appCache.appCache.get(AppCache.keys.ELEVEN_LABS_API.toString());

        
        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        
        Map<String, String> body = new HashMap<>();
        body.put("text", text);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        
        ResponseEntity<byte[]> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.POST,
                request,
                byte[].class
        );

        return response.getBody();
    }
}