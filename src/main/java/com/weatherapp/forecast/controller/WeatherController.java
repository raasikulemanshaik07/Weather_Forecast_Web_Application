package com.weatherapp.forecast.controller;

import com.weatherapp.forecast.dto.DailyForecast;
import com.weatherapp.forecast.dto.WeatherData;
import com.weatherapp.forecast.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam(name = "city", required = false, defaultValue = "London") String city,
                             Model model) {
        WeatherData weather = weatherService.getCurrentWeather(city);
        List<DailyForecast> forecast = weatherService.getFiveDayForecast(city);
        model.addAttribute("weather", weather);
        model.addAttribute("forecast", forecast);
        model.addAttribute("city", city);
        return "index";
    }
}
