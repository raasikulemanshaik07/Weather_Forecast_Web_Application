package com.weatherapp.forecast.exception;

public class CityNotFoundException extends RuntimeException {

    public CityNotFoundException(String cityName) {
        super("City not found: " + cityName);
    }
}
