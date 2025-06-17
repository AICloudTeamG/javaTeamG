package com.example.javaTeamG.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SalesPerformance") // DBテーブル名と一致させる
public class SalesPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @ManyToOne(fetch = FetchType.LAZY) // 必要に応じてEAGERに変更
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount;

    @ManyToOne(fetch = FetchType.LAZY) // 必要に応じてEAGERに変更
    @JoinColumn(name = "recorded_by_staff_id", nullable = false)
    private Staff recordedByStaff;

    @ManyToOne(fetch = FetchType.LAZY) // 必要に応じてEAGERに変更
    @JoinColumn(name = "sales_weather_id", nullable = false)
    private SalesWeather salesWeather;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public SalesPerformance() {} // JPAのためにデフォルトコンストラクタが必要

    // --- ゲッターとセッター ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getSalesCount() { return salesCount; }
    public void setSalesCount(Integer salesCount) { this.salesCount = salesCount; }
    public Staff getRecordedByStaff() { return recordedByStaff; }
    public void setRecordedByStaff(Staff recordedByStaff) { this.recordedByStaff = recordedByStaff; }
    public SalesWeather getSalesWeather() { return salesWeather; }
    public void setSalesWeather(SalesWeather salesWeather) { this.salesWeather = salesWeather; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

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