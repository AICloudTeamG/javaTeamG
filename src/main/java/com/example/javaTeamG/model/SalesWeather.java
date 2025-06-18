package com.example.javaTeamG.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_weather") // DBテーブル名と一致させる
public class SalesWeather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", unique = true, nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY) // 必要に応じてEAGERに変更
    @JoinColumn(name = "weather_code_id", nullable = false)
    private WeatherCode weatherCode;

    @Column(name = "temperature_max", nullable = false, precision = 4, scale = 1)
    private BigDecimal temperatureMax;

    @Column(name = "temperature_min", nullable = false, precision = 4, scale = 1)
    private BigDecimal temperatureMin;

    @Column(name = "temperature_mean", nullable = false, precision = 4, scale = 1)
    private BigDecimal temperatureMean;

    @Column(name = "humidity_mean", nullable = false, precision = 4, scale = 1)
    private BigDecimal humidityMean;

    @Column(name = "windspeed_max", nullable = false, precision = 4, scale = 1)
    private BigDecimal windspeedMax;

    @Column(name = "windspeed_mean", nullable = false, precision = 4, scale = 1)
    private BigDecimal windspeedMean;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public SalesWeather() {} // JPAのためにデフォルトコンストラクタが必要

    // --- ゲッターとセッター ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public WeatherCode getWeatherCode() { return weatherCode; }
    public void setWeatherCode(WeatherCode weatherCode) { this.weatherCode = weatherCode; }
    public BigDecimal getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(BigDecimal temperatureMax) { this.temperatureMax = temperatureMax; }
    public BigDecimal getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(BigDecimal temperatureMin) { this.temperatureMin = temperatureMin; }
    public BigDecimal getTemperatureMean() { return temperatureMean; }
    public void setTemperatureMean(BigDecimal temperatureMean) { this.temperatureMean = temperatureMean; }
    public BigDecimal getHumidityMean() { return humidityMean; }
    public void setHumidityMean(BigDecimal humidityMean) { this.humidityMean = humidityMean; }
    public BigDecimal getWindspeedMax() { return windspeedMax; }
    public void setWindspeedMax(BigDecimal windspeedMax) { this.windspeedMax = windspeedMax; }
    public BigDecimal getWindspeedMean() { return windspeedMean; }
    public void setWindspeedMean(BigDecimal windspeedMean) { this.windspeedMean = windspeedMean; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}