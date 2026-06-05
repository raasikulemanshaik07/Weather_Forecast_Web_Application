package com.weatherapp.forecast.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherapp.forecast.config.WeatherApiProperties;
import com.weatherapp.forecast.dto.DailyForecast;
import com.weatherapp.forecast.dto.WeatherData;
import com.weatherapp.forecast.exception.CityNotFoundException;
import com.weatherapp.forecast.exception.WeatherApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final WeatherApiProperties properties;
    private final ObjectMapper objectMapper;

    public WeatherService(RestTemplate restTemplate, WeatherApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    public WeatherData getCurrentWeather(String city) {
        double[] coords = getCoordinates(city);
        double lat = coords[0];
        double lon = coords[1];

        String url = String.format("%s/weather?lat=%f&lon=%f&appid=%s&units=metric",
                properties.getBaseUrl(), lat, lon, properties.getApiKey());

        try {
            String json = restTemplate.getForObject(url, String.class);
            Map<String, Object> response = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            Map<String, Object> main = (Map<String, Object>) response.get("main");
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            Map<String, Object> sys = (Map<String, Object>) response.get("sys");
            Map<String, Object> clouds = (Map<String, Object>) response.get("clouds");
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
            Map<String, Object> weather = weatherList.get(0);

            String cityName = (String) response.get("name");
            String country = (String) sys.get("country");
            String description = capitalizeWords((String) weather.get("description"));
            String icon = (String) weather.get("icon");
            double temperature = toDouble(main.get("temp"));
            double feelsLike = toDouble(main.get("feels_like"));
            double tempMin = toDouble(main.get("temp_min"));
            double tempMax = toDouble(main.get("temp_max"));
            int humidity = toInt(main.get("humidity"));
            int pressure = toInt(main.get("pressure"));
            double windSpeed = toDouble(wind.get("speed"));
            int windDegree = toInt(wind.get("deg"));
            int visibility = toInt(response.get("visibility"));

            long sunriseUnix = toLong(sys.get("sunrise"));
            long sunsetUnix = toLong(sys.get("sunset"));
            int timezoneOffset = toInt(response.get("timezone"));

            String sunrise = formatUnixTime(sunriseUnix, timezoneOffset);
            String sunset = formatUnixTime(sunsetUnix, timezoneOffset);

            int cloudiness = toInt(clouds.get("all"));

            return new WeatherData(cityName, country, description, icon,
                    temperature, feelsLike, tempMin, tempMax,
                    humidity, pressure, windSpeed, windDegree,
                    visibility, sunrise, sunset, cloudiness);

        } catch (CityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherApiException("Failed to fetch current weather data for: " + city, e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<DailyForecast> getFiveDayForecast(String city) {
        double[] coords = getCoordinates(city);
        double lat = coords[0];
        double lon = coords[1];

        String url = String.format("%s/forecast?lat=%f&lon=%f&appid=%s&units=metric",
                properties.getBaseUrl(), lat, lon, properties.getApiKey());

        try {
            String json = restTemplate.getForObject(url, String.class);
            Map<String, Object> response = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");

            String todayStr = LocalDate.now().toString();

            LinkedHashMap<String, List<Map<String, Object>>> groupedByDate = new LinkedHashMap<>();
            for (Map<String, Object> item : list) {
                String dtTxt = (String) item.get("dt_txt");
                String dateKey = dtTxt.substring(0, 10);

                if (dateKey.equals(todayStr)) {
                    continue;
                }

                groupedByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(item);
            }

            List<DailyForecast> forecasts = new ArrayList<>();
            int dayCount = 0;

            for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByDate.entrySet()) {
                if (dayCount >= 5) {
                    break;
                }

                String dateKey = entry.getKey();
                List<Map<String, Object>> items = entry.getValue();

                double maxTemp = Double.MIN_VALUE;
                double minTemp = Double.MAX_VALUE;
                double totalHumidity = 0;
                double totalWindSpeed = 0;

                Map<String, Integer> descriptionCount = new HashMap<>();
                Map<String, String> descriptionToIcon = new HashMap<>();

                for (Map<String, Object> item : items) {
                    Map<String, Object> main = (Map<String, Object>) item.get("main");
                    Map<String, Object> wind = (Map<String, Object>) item.get("wind");
                    List<Map<String, Object>> weatherList = (List<Map<String, Object>>) item.get("weather");
                    Map<String, Object> weather = weatherList.get(0);

                    double tempMaxVal = toDouble(main.get("temp_max"));
                    double tempMinVal = toDouble(main.get("temp_min"));

                    if (tempMaxVal > maxTemp) {
                        maxTemp = tempMaxVal;
                    }
                    if (tempMinVal < minTemp) {
                        minTemp = tempMinVal;
                    }

                    totalHumidity += toDouble(main.get("humidity"));
                    totalWindSpeed += toDouble(wind.get("speed"));

                    String desc = capitalizeWords((String) weather.get("description"));
                    String ico = (String) weather.get("icon");
                    descriptionCount.merge(desc, 1, Integer::sum);
                    descriptionToIcon.put(desc, ico);
                }

                int count = items.size();
                int avgHumidity = (int) Math.round(totalHumidity / count);
                double avgWindSpeed = Math.round((totalWindSpeed / count) * 100.0) / 100.0;

                String mostCommonDesc = "";
                int maxCount = 0;
                for (Map.Entry<String, Integer> descEntry : descriptionCount.entrySet()) {
                    if (descEntry.getValue() > maxCount) {
                        maxCount = descEntry.getValue();
                        mostCommonDesc = descEntry.getKey();
                    }
                }
                String mostCommonIcon = descriptionToIcon.getOrDefault(mostCommonDesc, "01d");

                LocalDate localDate = LocalDate.parse(dateKey);
                String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                String formattedDate = localDate.format(DateTimeFormatter.ofPattern("MMM d"));

                DailyForecast dailyForecast = new DailyForecast(dayName, formattedDate,
                        Math.round(maxTemp * 10.0) / 10.0,
                        Math.round(minTemp * 10.0) / 10.0,
                        mostCommonDesc, mostCommonIcon, avgHumidity, avgWindSpeed);

                forecasts.add(dailyForecast);
                dayCount++;
            }

            return forecasts;

        } catch (CityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherApiException("Failed to fetch forecast data for: " + city, e);
        }
    }

    @SuppressWarnings("unchecked")
    private double[] getCoordinates(String city) {
        String url = String.format("%s/direct?q=%s&limit=1&appid=%s",
                properties.getGeoUrl(), city, properties.getApiKey());

        try {
            String json = restTemplate.getForObject(url, String.class);
            List<Map<String, Object>> geoResults = objectMapper.readValue(json,
                    new TypeReference<List<Map<String, Object>>>() {});

            if (geoResults == null || geoResults.isEmpty()) {
                throw new CityNotFoundException(city);
            }

            Map<String, Object> location = geoResults.get(0);
            double lat = toDouble(location.get("lat"));
            double lon = toDouble(location.get("lon"));

            return new double[]{lat, lon};

        } catch (CityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherApiException("Failed to geocode city: " + city, e);
        }
    }

    private String formatUnixTime(long unixTimestamp, int timezoneOffsetSeconds) {
        return Instant.ofEpochSecond(unixTimestamp)
                .atZone(ZoneOffset.ofTotalSeconds(timezoneOffsetSeconds))
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(words[i].charAt(0)));
            if (words[i].length() > 1) {
                sb.append(words[i].substring(1));
            }
        }
        return sb.toString();
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
