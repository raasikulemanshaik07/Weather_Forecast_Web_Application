package com.weatherapp.forecast.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WeatherApiProperties {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.base-url}")
    private String baseUrl;

    @Value("${openweathermap.api.geo-url}")
    private String geoUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getGeoUrl() {
        return geoUrl;
    }
}
