package com.love.Backend.service;

import com.love.Backend.entity.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherService {
    @Value("${weather.api.key}")
    private String apiKey = "";

    private static final String API = "http://api.weatherapi.com/v1/current.json?key=API_KEY&query=CITY";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisService redisService;

    public WeatherResponse getWeather(String city){

        String key = "weather:" + city.toLowerCase();

        WeatherResponse weatherResponse = redisService.get(key, WeatherResponse.class);

        if(weatherResponse != null){
            return weatherResponse;
        }

        String url = API.replace("CITY", city).replace("API_KEY", apiKey);

        ResponseEntity<WeatherResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, WeatherResponse.class);

        WeatherResponse body = response.getBody();

        if(body != null){
            redisService.set(key, body, 300);
        }

        return body;
    }

}


