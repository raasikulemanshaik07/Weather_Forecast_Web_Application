package com.weatherapp.forecast.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CityNotFoundException.class)
    public String handleCityNotFound(CityNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "index";
    }

    @ExceptionHandler(WeatherApiException.class)
    public String handleWeatherApiException(WeatherApiException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("error", "An unexpected error occurred. Please try again later.");
        return "error";
    }
}
