package com.example.javaTeamG.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

// モデルクラスを PredictionData から OrderPredictionData に変更
public class OrderPredictionData {
    private LocalDate date;
    @JsonProperty("pale_ale")
    private Double paleAle;
    private Double lager;
    private Double ipa;
    private Double white;
    private Double dark;
    private Double fruit;
    @JsonProperty("temperature_2m_mean")
    private Double temperature2mMean;
    @JsonProperty("precipitation_sum")
    private Double precipitationSum;
    @JsonProperty("wind_speed_10m_mean")
    private Double windSpeed10mMean;
    private Double weekday;
    @JsonProperty("temp_diff")
    private Double tempDiff;
    @JsonProperty("temp_10d_avg")
    private Double temp10dAvg;
    @JsonProperty("weather_code")
    private Double weatherCode;
    @JsonProperty("shortwave_radiation_sum")
    private Double shortwaveRadiationSum;

    // デフォルトコンストラクタ (JSONパースに必要)
    public OrderPredictionData() {}

    // コンストラクタ (テストや手動生成用)
    public OrderPredictionData(LocalDate date, Double paleAle, Double lager, Double ipa, Double white, Double dark, Double fruit, Double temperature2mMean, Double precipitationSum, Double windSpeed10mMean, Double weekday, Double tempDiff, Double temp10dAvg, Double weatherCode, Double shortwaveRadiationSum) {
        this.date = date;
        this.paleAle = paleAle;
        this.lager = lager;
        this.ipa = ipa;
        this.white = white;
        this.dark = dark;
        this.fruit = fruit;
        this.temperature2mMean = temperature2mMean;
        this.precipitationSum = precipitationSum;
        this.windSpeed10mMean = windSpeed10mMean;
        this.weekday = weekday;
        this.tempDiff = tempDiff;
        this.temp10dAvg = temp10dAvg;
        this.weatherCode = weatherCode;
        this.shortwaveRadiationSum = shortwaveRadiationSum;
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

    public Double getPrecipitationSum() { return precipitationSum; }
    public void setPrecipitationSum(Double precipitationSum) { this.precipitationSum = precipitationSum; }

    public Double getWindSpeed10mMean() { return windSpeed10mMean; }
    public void setWindSpeed10mMean(Double windSpeed10mMean) { this.windSpeed10mMean = windSpeed10mMean; }

    public Double getWeekday() { return weekday; }
    public void setWeekday(Double weekday) { this.weekday = weekday; }

    public Double getTempDiff() { return tempDiff; }
    public void setTempDiff(Double tempDiff) { this.tempDiff = tempDiff; }

    public Double getTemp10dAvg() { return temp10dAvg; }
    public void setTemp10dAvg(Double temp10dAvg) { this.temp10dAvg = temp10dAvg; }

    public Double getWeatherCode() { return weatherCode; }
    public void setWeatherCode(Double weatherCode) { this.weatherCode = weatherCode; }

    public Double getShortwaveRadiationSum() { return shortwaveRadiationSum; }
    public void setShortwaveRadiationSum(Double shortwaveRadiationSum) { this.shortwaveRadiationSum = shortwaveRadiationSum; }
}