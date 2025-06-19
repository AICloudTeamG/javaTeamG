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

    @Column(name = "date", unique = true, nullable = false) // 日付 (ユニークキー)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY) // 必要に応じてEAGERに変更
    @JoinColumn(name = "weather_code_id", nullable = false) // 天気コードID (weather_codesテーブルのidを参照)
    private WeatherCode weatherCode;

    @Column(name = "temperature_max", nullable = false, precision = 4, scale = 1) // 最高気温 (℃)
    private BigDecimal temperatureMax;

    @Column(name = "temperature_mean", nullable = false, precision = 4, scale = 1) // 最低気温 (℃)
    private BigDecimal temperatureMean; // 平均気温 (℃) - 元のDB定義に'temperature_mean'があるため保持
   
    @Column(name = "temperature_min", nullable = false, precision = 4, scale = 1) // 最低気温 (℃)
    private BigDecimal temperatureMin; // 平均気温 (℃) - 元のDB定義に'temperature_mean'があるため保持

    @Column(name = "humidity_max", nullable = false, precision = 4, scale = 1) // 最大湿度 (%)
    private BigDecimal humidityMax;

    @Column(name = "humidity_min", nullable = false, precision = 4, scale = 1) // 最低湿度 (%)
    private BigDecimal humidityMin;

    @Column(name = "windspeed_max", nullable = false, precision = 4, scale = 1) // 最大風速 (km/h)
    private BigDecimal windspeedMax;

    // DB定義に 'windspeed_mean' がないため、このフィールドは削除します。
    // DB定義に 'humidity_mean' がないため、このフィールドは削除します。


    @Column(name = "created_at", nullable = false, updatable = false) // レコード作成日時
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // レコード更新日時 (default: `now()` は@PreUpdateで対応)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false) // 論理削除フラグ (true: 削除済み, false: 未削除)
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

    public BigDecimal getHumidityMax() { return humidityMax; }
    public void setHumidityMax(BigDecimal humidityMax) { this.humidityMax = humidityMax; }
    public BigDecimal getHumidityMin() { return humidityMin; }
    public void setHumidityMin(BigDecimal humidityMin) { this.humidityMin = humidityMin; }

    public BigDecimal getWindspeedMax() { return windspeedMax; }
    public void setWindspeedMax(BigDecimal windspeedMax) { this.windspeedMax = windspeedMax; }

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