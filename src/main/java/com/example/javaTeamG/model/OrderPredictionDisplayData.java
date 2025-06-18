package com.example.javaTeamG.model;

import java.time.LocalDate;

public class OrderPredictionDisplayData {
    private LocalDate orderDate; // 発注日
    private LocalDate startDate; // 発注対象期間の開始日
    private LocalDate endDate;   // 発注対象期間の終了日
    private Double paleAleSum;
    private Double lagerSum;
    private Double ipaSum;
    private Double whiteSum;
    private Double darkSum;
    private Double fruitSum;

    // コンストラクタ
    public OrderPredictionDisplayData(LocalDate orderDate, LocalDate startDate, LocalDate endDate,
                                      Double paleAleSum, Double lagerSum, Double ipaSum,
                                      Double whiteSum, Double darkSum, Double fruitSum) {
        this.orderDate = orderDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.paleAleSum = paleAleSum;
        this.lagerSum = lagerSum;
        this.ipaSum = ipaSum;
        this.whiteSum = whiteSum;
        this.darkSum = darkSum;
        this.fruitSum = fruitSum;
    }

    // Getters
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Double getPaleAleSum() { return paleAleSum; }
    public Double getLagerSum() { return lagerSum; }
    public Double getIpaSum() { return ipaSum; }
    public Double getWhiteSum() { return whiteSum; }
    public Double getDarkSum() { return darkSum; }
    public Double getFruitSum() { return fruitSum; }

    // Setters (必要であれば追加)
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setPaleAleSum(Double paleAleSum) { this.paleAleSum = paleAleSum; }
    public void setLagerSum(Double lagerSum) { this.lagerSum = lagerSum; }
    public void setIpaSum(Double ipaSum) { this.ipaSum = ipaSum; }
    public void setWhiteSum(Double whiteSum) { this.whiteSum = whiteSum; }
    public void setDarkSum(Double darkSum) { this.darkSum = darkSum; }
    public void setFruitSum(Double fruitSum) { this.fruitSum = fruitSum; }
}