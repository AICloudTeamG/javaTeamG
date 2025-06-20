// src/main/java/com/example/javaTeamG/service/DataVisualizationService.java
package com.example.javaTeamG.service;

import com.example.javaTeamG.model.SalesWeatherChartData;
import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.model.SalesPerformance;
import com.example.javaTeamG.model.SalesWeather;
import com.example.javaTeamG.repository.ProductRepository;
import com.example.javaTeamG.repository.SalesPerformanceRepository;
import com.example.javaTeamG.repository.SalesWeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DataVisualizationService {

    private final SalesPerformanceRepository salesPerformanceRepository;
    private final SalesWeatherRepository salesWeatherRepository;
    private final ProductRepository productRepository;

    public DataVisualizationService(SalesPerformanceRepository salesPerformanceRepository,
                                    SalesWeatherRepository salesWeatherRepository,
                                    ProductRepository productRepository) {
        this.salesPerformanceRepository = salesPerformanceRepository;
        this.salesWeatherRepository = salesWeatherRepository;
        this.productRepository = productRepository;
    }

    /**
     * 指定された週の天気と販売実績データを取得・集計します。
     * @param startDate 週の開始日（月曜日）
     * @return 7日分のSalesWeatherChartDataのリスト
     */
    public List<SalesWeatherChartData> getWeeklySalesAndWeatherData(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        List<SalesWeatherChartData> weeklyData = new ArrayList<>();

        // 期間内の天気データを一括取得
        Map<LocalDate, SalesWeather> weatherMap = salesWeatherRepository.findByDateBetween(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(SalesWeather::getDate, sw -> sw));

        // 期間内の販売実績データを一括取得
        List<SalesPerformance> performances = salesPerformanceRepository.findByRecordDateBetween(startDate, endDate);
        // 日付ごとの販売実績をマップに整理
        Map<LocalDate, List<SalesPerformance>> dailyPerformancesMap = performances.stream()
                .collect(Collectors.groupingBy(SalesPerformance::getRecordDate));

        // 全商品の単価をマップで取得（Product.getPrice()がInteger型になったため、Map<String, Integer>で受け取る）
        Map<String, Integer> productPrices = productRepository.findByIsDeletedFalse().stream()
                .collect(Collectors.toMap(Product::getName, Product::getPrice));

        // 週の各日についてデータを生成
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            String dayOfWeekName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.JAPANESE);

            SalesWeather salesWeather = weatherMap.get(currentDate);
            List<SalesPerformance> dailyPerf = dailyPerformancesMap.getOrDefault(currentDate, new ArrayList<>());

            // デフォルト値
            String weatherCondition = "不明";
            BigDecimal tempMax = BigDecimal.ZERO;
            BigDecimal tempMin = BigDecimal.ZERO;
            BigDecimal tempMean = BigDecimal.ZERO;
            BigDecimal humidityMax = BigDecimal.ZERO;
            BigDecimal humidityMin = BigDecimal.ZERO;
            BigDecimal windspeedMax = BigDecimal.ZERO;

            if (salesWeather != null) {
                weatherCondition = convertWeatherCodeToEmoji(salesWeather.getWeatherCode().getId());
                weatherCondition += " " + salesWeather.getWeatherCode().getDescription();
                tempMax = salesWeather.getTemperatureMax();
                tempMin = salesWeather.getTemperatureMin();
                tempMean = salesWeather.getTemperatureMean();
                humidityMax = salesWeather.getHumidityMax();
                humidityMin = salesWeather.getHumidityMin();
                windspeedMax = salesWeather.getWindspeedMax();
            }

            // ★修正点: salesQuantitiesは数量なのでInteger、salesAmountsは金額なのでBigDecimalで管理★
            Map<String, Integer> salesQuantities = new HashMap<>();
            Map<String, BigDecimal> salesAmounts = new HashMap<>(); // 金額はBigDecimalで管理
            BigDecimal totalSalesAmount = BigDecimal.ZERO; // 総売上額もBigDecimalで管理

            for (SalesPerformance sp : dailyPerf) {
                String productName = sp.getProduct().getName();
                Integer quantity = sp.getSalesCount();
                
                // ★修正点: productPricesからIntegerで価格を取得し、BigDecimalに変換して計算★
                // productPricesマップはIntegerを返すので、getOrDefaultのデフォルト値もIntegerにする
                Integer productPriceInteger = productPrices.getOrDefault(productName, 0); 
                
                // 計算はBigDecimalで行う
                BigDecimal productPriceBigDecimal = new BigDecimal(productPriceInteger);
                BigDecimal quantityBigDecimal = new BigDecimal(quantity);
                
                BigDecimal amount = productPriceBigDecimal.multiply(quantityBigDecimal); // BigDecimal同士の乗算

                salesQuantities.merge(productName, quantity, Integer::sum);
                salesAmounts.merge(productName, amount, BigDecimal::add); // BigDecimal同士の加算
                totalSalesAmount = totalSalesAmount.add(amount); // BigDecimal同士の加算
            }

            weeklyData.add(new SalesWeatherChartData(
                    currentDate,
                    dayOfWeekName,
                    weatherCondition,
                    tempMax,
                    tempMin,
                    tempMean,
                    humidityMax,
                    humidityMin,
                    windspeedMax,
                    salesQuantities,
                    salesAmounts,
                    totalSalesAmount
            ));
        }

        return weeklyData;
    }

    /**
     * WMO天気コードを絵文字と説明に変換するヘルパーメソッド
     * @param weatherCodeId WMO天気コードID
     * @return 絵文字と説明の文字列
     */
    public String convertWeatherCodeToEmoji(Integer weatherCodeId) {
        // 実際のWMOコードと絵文字の対応は、Open-Meteoのドキュメントなどを参照して実装
        // https://open-meteo.com/en/docs
        return switch (weatherCodeId) {
            case 0 -> "☀️"; // Clear sky
            case 1, 2, 3 -> "☁️"; // Mainly clear, partly cloudy, overcast
            case 45, 48 -> "🌫️"; // Fog, depositing rime fog
            case 51, 53, 55 -> "🌧️"; // Drizzle: Light, moderate, and dense intensity
            case 56, 57 -> "🌨️"; // Freezing Drizzle: Light and dense intensity
            case 61, 63, 65 -> "🌧️"; // Rain: Slight, moderate and heavy intensity
            case 66, 67 -> "🌨️"; // Freezing Rain: Light and heavy intensity
            case 71, 73, 75 -> "🌨️"; // Snow fall: Slight, moderate, and heavy intensity
            case 77 -> "❄️"; // Snow grains
            case 80, 81, 82 -> "⛈️"; // Rain showers: Slight, moderate, and violent
            case 85, 86 -> "🌨️"; // Snow showers slight and heavy
            case 95 -> "⚡"; // Thunderstorm: Slight or moderate
            case 96, 99 -> "🌪️"; // Thunderstorm with slight and heavy hail
            default -> "❓"; // Unknown
        };
    }

    /**
     * 特定の日付の天気情報を取得します（ポップアップ表示用）
     * @param date 日付
     * @return SalesWeatherオブジェクト（存在しない場合はOptional.empty()）
     */
    public Optional<SalesWeather> getSalesWeatherForDate(LocalDate date) {
        return salesWeatherRepository.findByDateAndIsDeletedFalse(date);
    }
}