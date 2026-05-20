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
public class TextToSpeechService {

    @Value("${murf.api.key}")
    private String murfApiKey;

    @Value("${murf.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] convertTextToSpeech(String text) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", murfApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("voiceId", "Matthew");
        body.put("model", "FALCON");
        body.put("locale", "en-US");
        body.put("sampleRate", 24000);
        body.put("format", "MP3");

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                request,
                byte[].class
        );

        return response.getBody();
    }
}