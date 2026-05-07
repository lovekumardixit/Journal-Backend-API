package com.love.Backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


import java.util.Currency;
import java.util.List;
@Getter
@Setter
public class WeatherResponse {
    private Current current;


    @Getter
    @Setter
    public static class Current{
        @JsonProperty("temp_c")
        private double tempC;

        @JsonProperty("feelslike_c")
        private double feelsLikeC;
    }
}
