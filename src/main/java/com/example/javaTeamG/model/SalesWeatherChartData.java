// src/main/java/com/example/javaTeamG/model/SalesWeatherChartData.java
package com.example.javaTeamG.model; // パッケージを model に変更

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class SalesWeatherChartData {
    private LocalDate date;
    private String dayOfWeek;
    private String weatherCondition; // "☀️ 晴れ" のような文字列
    private BigDecimal temperatureMax;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMean;
    private BigDecimal humidityMax;
    private BigDecimal humidityMin;
    private BigDecimal windspeedMax;
    private Map<String, Integer> salesQuantities; // 商品名 -> 販売本数
    private Map<String, BigDecimal> salesAmounts; // 商品名 -> 販売金額合計
    private BigDecimal totalSalesAmount; // その日の全商品の合計売上金額

    // コンストラクタ、ゲッター、セッター
    public SalesWeatherChartData(LocalDate date, String dayOfWeek, String weatherCondition,
                                 BigDecimal temperatureMax, BigDecimal temperatureMin, BigDecimal temperatureMean,
                                 BigDecimal humidityMax, BigDecimal humidityMin, BigDecimal windspeedMax,
                                 Map<String, Integer> salesQuantities, Map<String, BigDecimal> salesAmounts, BigDecimal totalSalesAmount) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.weatherCondition = weatherCondition;
        this.temperatureMax = temperatureMax;
        this.temperatureMin = temperatureMin;
        this.temperatureMean = temperatureMean;
        this.humidityMax = humidityMax;
        this.humidityMin = humidityMin;
        this.windspeedMax = windspeedMax;
        this.salesQuantities = salesQuantities;
        this.salesAmounts = salesAmounts;
        this.totalSalesAmount = totalSalesAmount;
    }

    // Getters
    public LocalDate getDate() { return date; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getWeatherCondition() { return weatherCondition; }
    public BigDecimal getTemperatureMax() { return temperatureMax; }
    public BigDecimal getTemperatureMin() { return temperatureMin; }
    public BigDecimal getTemperatureMean() { return temperatureMean; }
    public BigDecimal getHumidityMax() { return humidityMax; }
    public BigDecimal getHumidityMin() { return humidityMin; }
    public BigDecimal getWindspeedMax() { return windspeedMax; }
    public Map<String, Integer> getSalesQuantities() { return salesQuantities; }
    public Map<String, BigDecimal> getSalesAmounts() { return salesAmounts; }
    public BigDecimal getTotalSalesAmount() { return totalSalesAmount; }

    // Setters (必要であれば)
    public void setDate(LocalDate date) { this.date = date; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    public void setTemperatureMax(BigDecimal temperatureMax) { this.temperatureMax = temperatureMax; }
    public void setTemperatureMin(BigDecimal temperatureMin) { this.temperatureMin = temperatureMin; }
    public void setTemperatureMean(BigDecimal temperatureMean) { this.temperatureMean = temperatureMean; }
    public void setHumidityMax(BigDecimal humidityMax) { this.humidityMax = humidityMax; }
    public void setHumidityMin(BigDecimal humidityMin) { this.humidityMin = humidityMin; }
    public void setWindspeedMax(BigDecimal windspeedMax) { this.windspeedMax = windspeedMax; }
    public void setSalesQuantities(Map<String, Integer> salesQuantities) { this.salesQuantities = salesQuantities; }
    public void setSalesAmounts(Map<String, BigDecimal> salesAmounts) { this.salesAmounts = salesAmounts; }
    public void setTotalSalesAmount(BigDecimal totalSalesAmount) { this.totalSalesAmount = totalSalesAmount; }
}