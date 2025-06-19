package com.example.javaTeamG.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class OrderPredictionData {
    private LocalDate date;
    @JsonProperty("pale_ale")//表記が違うものだけアノテーション
    private Double paleAle;
    private Double lager;
    private Double ipa;
    private Double white;
    private Double dark;
    private Double fruit;
    @JsonProperty("temperature_2m_mean")
    private Double temperature2mMean;
    @JsonProperty("weather_code")
    private Double weatherCode;
    @JsonProperty("temperature_2m_max")
    private Double temperature2mMax;
    @JsonProperty("temperature_2m_min")
    private Double temperature2mMin;
    @JsonProperty("wind_speed_10m_max")
    private Double windSpeed10mMax; 
    @JsonProperty("relative_humidity_2m_max")
    private Double relativeHumidity2mMax;
    @JsonProperty("relative_humidity_2m_min")
    private Double relativeHumidity2mMin;
    private Double weekday;

    // デフォルトコンストラクタ 
    public OrderPredictionData() {}

    // コンストラクタ 
    public OrderPredictionData(LocalDate date, Double paleAle, Double lager, Double ipa, Double white, Double dark, Double fruit,
                               Double temperature2mMean, Double weatherCode,
                               Double temperature2mMax, Double temperature2mMin, Double windSpeed10mMax,
                               Double relativeHumidity2mMax, Double relativeHumidity2mMin,
                               Double weekday) {
        this.date = date;
        this.paleAle = paleAle;
        this.lager = lager;
        this.ipa = ipa;
        this.white = white;
        this.dark = dark;
        this.fruit = fruit;
        this.temperature2mMean = temperature2mMean;
        this.weatherCode = weatherCode;
        this.temperature2mMax = temperature2mMax;
        this.temperature2mMin = temperature2mMin;
        this.windSpeed10mMax = windSpeed10mMax;
        this.relativeHumidity2mMax = relativeHumidity2mMax;
        this.relativeHumidity2mMin = relativeHumidity2mMin;
        this.weekday = weekday;
    }

    // Getters and Setters

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getPaleAle() { return paleAle; }
    public void setPaleAle(Double paleAle) { this.paleAle = paleAle; }

    public Double getLager() { return lager; }
    public void setLager(Double lager) { this.lager = lager; }

    public Double getIpa() { return ipa; }
    public void setIpa(Double ipa) { this.ipa = ipa; }

    public Double getWhite() { return white; }
    public void setWhite(Double white) { this.white = white; }

    public Double getDark() { return dark; }
    public void setDark(Double dark) { this.dark = dark; }

    public Double getFruit() { return fruit; }
    public void setFruit(Double fruit) { this.fruit = fruit; }

    public Double getTemperature2mMean() { return temperature2mMean; }
    public void setTemperature2mMean(Double temperature2mMean) { this.temperature2mMean = temperature2mMean; }

    public Double getWeatherCode() { return weatherCode; }
    public void setWeatherCode(Double weatherCode) { this.weatherCode = weatherCode; }

    public Double getTemperature2mMax() { return temperature2mMax; }
    public void setTemperature2mMax(Double temperature2mMax) { this.temperature2mMax = temperature2mMax; }

    public Double getTemperature2mMin() { return temperature2mMin; }
    public void setTemperature2mMin(Double temperature2mMin) { this.temperature2mMin = temperature2mMin; }

    public Double getWindSpeed10mMax() { return windSpeed10mMax; }
    public void setWindSpeed10mMax(Double windSpeed10mMax) { this.windSpeed10mMax = windSpeed10mMax; }

    public Double getRelativeHumidity2mMax() { return relativeHumidity2mMax; }
    public void setRelativeHumidity2mMax(Double relativeHumidity2mMax) { this.relativeHumidity2mMax = relativeHumidity2mMax; }

    public Double getRelativeHumidity2mMin() { return relativeHumidity2mMin; }
    public void setRelativeHumidity2mMin(Double relativeHumidity2mMin) { this.relativeHumidity2mMin = relativeHumidity2mMin; }

    public Double getWeekday() { return weekday; }
    public void setWeekday(Double weekday) { this.weekday = weekday; }
}