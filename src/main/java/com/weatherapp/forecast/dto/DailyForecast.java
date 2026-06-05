package com.weatherapp.forecast.dto;

public class DailyForecast {

    private String dayName;
    private String date;
    private double tempMax;
    private double tempMin;
    private String description;
    private String icon;
    private int humidity;
    private double windSpeed;

    public DailyForecast() {
    }

    public DailyForecast(String dayName, String date, double tempMax, double tempMin,
                         String description, String icon, int humidity, double windSpeed) {
        this.dayName = dayName;
        this.date = date;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.description = description;
        this.icon = icon;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
}
