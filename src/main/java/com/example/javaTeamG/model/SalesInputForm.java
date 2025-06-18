// src/main/java/com/example/javaTeamG/model/SalesInputForm.java
package com.example.javaTeamG.model;

import java.time.LocalDate;
import java.util.List;

public class SalesInputForm {
    private LocalDate date;
    private Integer recorderId; // HTMLのhidden input fieldに対応
    private List<ProductSalesEntry> performances; // 各商品の販売数データ

    // GetterとSetter

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getRecorderId() {
        return recorderId;
    }

    public void setRecorderId(Integer recorderId) {
        this.recorderId = recorderId;
    }

    public List<ProductSalesEntry> getPerformances() {
        return performances;
    }

    public void setPerformances(List<ProductSalesEntry> performances) {
        this.performances = performances;
    }

    @Override
    public String toString() {
        return "SalesInputForm{" +
               "date=" + date +
               ", recorderId=" + recorderId +
               ", performances=" + performances +
               '}';
    }
}