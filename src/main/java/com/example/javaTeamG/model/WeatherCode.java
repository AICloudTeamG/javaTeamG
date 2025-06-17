package com.example.javaTeamG.model;

import jakarta.persistence.*;

@Entity
@Table(name = "WeatherCodes") // DBテーブル名と一致させる
public class WeatherCode {
    @Id
    private Integer id; // WMO準拠コードをそのままIDとして使用

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public WeatherCode() {} // JPAのためにデフォルトコンストラクタが必要

    // --- ゲッターとセッター ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
}