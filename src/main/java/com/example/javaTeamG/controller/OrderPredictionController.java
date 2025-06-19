package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.OrderPredictionData;
import com.example.javaTeamG.model.OrderPredictionDisplayData;
import com.example.javaTeamG.service.OrderPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderPredictionController {

    private final OrderPredictionService orderPredictionService;
    //ここを本物のAPIに変えるべき！！！！！
    private static final String EXTERNAL_PREDICTION_API_URL = "/dummy-order-prediction-data";
    // 予測可能な最大日数 (16日先まで)
    private static final int MAX_PREDICTION_DAYS = 16;

    @Autowired
    public OrderPredictionController(OrderPredictionService orderPredictionService) {
        this.orderPredictionService = orderPredictionService;
    }

    @GetMapping("/admin/prediction")
    public String showPrediction(Model model) { 
        List<OrderPredictionData> allPredictions;
        try {
            String dummyJsonData = getDummyPredictionDataString();
            allPredictions = orderPredictionService.parsePredictionData(dummyJsonData);

        } catch (IOException e) {
            System.err.println("Failed to fetch or parse prediction data: " + e.getMessage());
            model.addAttribute("errorMessage", "予測データの取得または処理に失敗しました。");
            return "error";
        }

        if (allPredictions.isEmpty()) {
            model.addAttribute("predictionListForDisplay", new ArrayList<>());
            model.addAttribute("pageTitle", "発注予測");
            return "admin/order-prediction";
        }

        // 日付でソート
        allPredictions.sort(Comparator.comparing(OrderPredictionData::getDate));

        // 直近4回分までの発注日と対象期間の組み合わせを生成
        List<OrderDateRange> orderDateRangesToShow = generateOrderDateRanges(allPredictions); // メソッド名はそのままでロジック変更

        List<OrderPredictionDisplayData> predictionListForDisplay = new ArrayList<>();//jsonで受け取った型をそのまま

        for (OrderDateRange range : orderDateRangesToShow) {
            LocalDate orderDate = range.getOrderDate();
            LocalDate startDate = range.getStartDate();
            LocalDate endDate = range.getEndDate();

            // 対象期間内の予測データをフィルタリング
            List<OrderPredictionData> relevantPredictions = allPredictions.stream()
                    .filter(data -> !data.getDate().isBefore(startDate) && !data.getDate().isAfter(endDate))
                    .collect(Collectors.toList());

            // 日曜日を除外して合計を計算
            double paleAleSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getPaleAle)
                    .sum();
            double lagerSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getLager)
                    .sum();
            double ipaSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getIpa)
                    .sum();
            double whiteSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getWhite)
                    .sum();
            double darkSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getDark)
                    .sum();
            double fruitSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getFruit)
                    .sum();

            predictionListForDisplay.add(new OrderPredictionDisplayData(
                    orderDate, startDate, endDate,
                    paleAleSum, lagerSum, ipaSum, whiteSum, darkSum, fruitSum
            ));
        }

        model.addAttribute("predictionListForDisplay", predictionListForDisplay);
        model.addAttribute("pageTitle", "発注予測");

        return "admin/order-prediction";
    }

    public static class OrderDateRange {
        private LocalDate orderDate;
        private LocalDate startDate;
        private LocalDate endDate;

        public OrderDateRange(LocalDate orderDate, LocalDate startDate, LocalDate endDate) {
            this.orderDate = orderDate;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDate getOrderDate() { return orderDate; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }

    private List<OrderDateRange> generateOrderDateRanges(List<OrderPredictionData> allPredictions) {
        List<OrderDateRange> potentialRanges = new ArrayList<>();
        if (allPredictions.isEmpty()) {
            return potentialRanges;
        }

        LocalDate firstPredictionDate = allPredictions.get(0).getDate();
        LocalDate lastPredictionDate = allPredictions.get(allPredictions.size() - 1).getDate();

        LocalDate today = LocalDate.now();


        // 今日からMAX_PREDICTION_DAYS最大予測可能日（16日後）までループし、全ての発注可能日を候補として抽出
        for (int i = 0; i <= MAX_PREDICTION_DAYS; i++) { // 16日先まで
            LocalDate orderDate = today.plusDays(i);
            LocalDate currentStartDate = null;
            LocalDate currentEndDate = null;

            if (orderDate.getDayOfWeek() == DayOfWeek.MONDAY) {
                currentStartDate = orderDate.plusDays(1); // 火曜日
                currentEndDate = orderDate.plusWeeks(1);  // 翌週の月曜日
            } else if (orderDate.getDayOfWeek() == DayOfWeek.THURSDAY) {
                currentStartDate = orderDate.plusDays(1); // 金曜日
                currentEndDate = orderDate.plusWeeks(1).with(DayOfWeek.MONDAY); // 翌週の月曜日
            }

            if (currentStartDate != null && currentEndDate != null) {
                // APIデータが16日先までしか提供されないことを考慮し、必要なデータが全てAPIの範囲内にあるかを確認
                // final変数の制約対応
                final LocalDate finalStartDate = currentStartDate;
                final LocalDate finalEndDate = currentEndDate;

                // 必要な期間のデータがallPredictions内に全て含まれているかを確認
                // APIの予測はfirstPredictionDateからlastPredictionDateまで
                boolean hasAllRequiredPredictionData =
                        !finalStartDate.isBefore(firstPredictionDate) &&
                        !finalEndDate.isAfter(lastPredictionDate) &&
                        !finalStartDate.isAfter(finalEndDate); // 期間の開始日が終了日より後ではないこと

                if (hasAllRequiredPredictionData) {
                    potentialRanges.add(new OrderDateRange(orderDate, finalStartDate, finalEndDate));
                }
            }
        }

        // 発注日でソート
        potentialRanges.sort(Comparator.comparing(OrderDateRange::getOrderDate));

        // 今日以降の発注日のみを抽出して返す
        return potentialRanges.stream()
                .filter(range -> !range.getOrderDate().isBefore(today)) // 今日以降
                .limit(4) // 最大４つ
                .collect(Collectors.toList());
    }


    //開発用
    @GetMapping(EXTERNAL_PREDICTION_API_URL)
    @ResponseBody
    public List<OrderPredictionData> getDummyPredictionDataForApi() {
        try {
            return orderPredictionService.parsePredictionData(getDummyPredictionDataString());
        } catch (IOException e) {
            System.err.println("Failed to parse dummy data for API endpoint: " + e.getMessage());
            return List.of();
        }
    }

    // ダミーデータ
    private String getDummyPredictionDataString() {
        return """
            [{"date": "2025-06-18", "pale_ale": 100, "lager": 8.06, "ipa": 4.82, "white": 5.01, "dark": 3.11, "fruit": 3.72, "temperature_2m_mean": 27.5, "weather_code": 1.0, "temperature_2m_max": 33.0, "temperature_2m_min": 23.0, "wind_speed_10m_max": 7.1, "relative_humidity_2m_max": 93.0, "relative_humidity_2m_min": 50.0, "weekday": 2.0},
            {"date": "2025-06-19", "pale_ale": 0, "lager": 8.55, "ipa": 5.25, "white": 5.32, "dark": 3.33, "fruit": 4.14, "temperature_2m_mean": 26.4, "weather_code": 3.0, "temperature_2m_max": 30.8, "temperature_2m_min": 23.4, "wind_speed_10m_max": 7.1, "relative_humidity_2m_max": 98.0, "relative_humidity_2m_min": 60.0, "weekday": 3.0},
            {"date": "2025-06-20", "pale_ale": 1000, "lager": 11.23, "ipa": 7.49, "white": 6.7, "dark": 4.73, "fruit": 4.35, "temperature_2m_mean": 25.5, "weather_code": 2.0, "temperature_2m_max": 29.6, "temperature_2m_min": 21.9, "wind_speed_10m_max": 7.4, "relative_humidity_2m_max": 97.0, "relative_humidity_2m_min": 65.0, "weekday": 4.0},
            {"date": "2025-06-21", "pale_ale": 5.76, "lager": 7.38, "ipa": 4.58, "white": 5.13, "dark": 2.67, "fruit": 4.03, "temperature_2m_mean": 25.4, "weather_code": 3.0, "temperature_2m_max": 29.4, "temperature_2m_min": 22.2, "wind_speed_10m_max": 18.7, "relative_humidity_2m_max": 95.0, "relative_humidity_2m_min": 55.0, "weekday": 5.0},
            {"date": "2025-06-22", "pale_ale": 6.21, "lager": 7.32, "ipa": 4.55, "white": 4.46, "dark": 2.85, "fruit": 3.89, "temperature_2m_mean": 26.3, "weather_code": 3.0, "temperature_2m_max": 30.9, "temperature_2m_min": 22.5, "wind_speed_10m_max": 24.3, "relative_humidity_2m_max": 88.0, "relative_humidity_2m_min": 53.0, "weekday": 6.0},
            {"date": "2025-06-23", "pale_ale": 5.7, "lager": 6.05, "ipa": 3.72, "white": 3.49, "dark": 2.47, "fruit": 2.62, "temperature_2m_mean": 24.8, "weather_code": 61.0, "temperature_2m_max": 26.6, "temperature_2m_min": 23.7, "wind_speed_10m_max": 14.8, "relative_humidity_2m_max": 89.0, "relative_humidity_2m_min": 71.0, "weekday": 0.0},
            {"date": "2025-06-24", "pale_ale": 5.3, "lager": 6.68, "ipa": 4.23, "white": 4.37, "dark": 2.79, "fruit": 3.09, "temperature_2m_mean": 26.4, "weather_code": 3.0, "temperature_2m_max": 30.7, "temperature_2m_min": 23.1, "wind_speed_10m_max": 17.7, "relative_humidity_2m_max": 92.0, "relative_humidity_2m_min": 59.0, "weekday": 1.0},
            {"date": "2025-06-25", "pale_ale": 5.47, "lager": 6.49, "ipa": 3.44, "white": 4.37, "dark": 2.74, "fruit": 3.06, "temperature_2m_mean": 26.7, "weather_code": 3.0, "temperature_2m_max": 29.5, "temperature_2m_min": 24.3, "wind_speed_10m_max": 16.5, "relative_humidity_2m_max": 92.0, "relative_humidity_2m_min": 64.0, "weekday": 2.0},
            {"date": "2025-06-26", "pale_ale": 5.27, "lager": 7.15, "ipa": 3.79, "white": 4.59, "dark": 2.39, "fruit": 2.66, "temperature_2m_mean": 27.9, "weather_code": 51.0, "temperature_2m_max": 31.9, "temperature_2m_min": 24.7, "wind_speed_10m_max": 21.2, "relative_humidity_2m_max": 77.0, "relative_humidity_2m_min": 47.0, "weekday": 3.0},
            {"date": "2025-06-27", "pale_ale": 5.5, "lager": 7.0, "ipa": 4.0, "white": 4.5, "dark": 2.5, "fruit": 2.8, "temperature_2m_mean": 28.0, "weather_code": 1.0, "temperature_2m_max": 32.0, "temperature_2m_min": 25.0, "wind_speed_10m_max": 15.0, "relative_humidity_2m_max": 80.0, "relative_humidity_2m_min": 45.0, "weekday": 4.0},
            {"date": "2025-06-28", "pale_ale": 6.0, "lager": 7.5, "ipa": 4.5, "white": 5.0, "dark": 2.8, "fruit": 3.0, "temperature_2m_mean": 29.0, "weather_code": 2.0, "temperature_2m_max": 33.0, "temperature_2m_min": 26.0, "wind_speed_10m_max": 16.0, "relative_humidity_2m_max": 75.0, "relative_humidity_2m_min": 40.0, "weekday": 5.0},
            {"date": "2025-06-29", "pale_ale": 6.5, "lager": 8.0, "ipa": 5.0, "white": 5.5, "dark": 3.0, "fruit": 3.2, "temperature_2m_mean": 30.0, "weather_code": 3.0, "temperature_2m_max": 34.0, "temperature_2m_min": 27.0, "wind_speed_10m_max": 17.0, "relative_humidity_2m_max": 70.0, "relative_humidity_2m_min": 35.0, "weekday": 6.0},
            {"date": "2025-06-30", "pale_ale": 7.0, "lager": 8.5, "ipa": 5.5, "white": 6.0, "dark": 3.2, "fruit": 3.5, "temperature_2m_mean": 31.0, "weather_code": 1.0, "temperature_2m_max": 35.0, "temperature_2m_min": 28.0, "wind_speed_10m_max": 18.0, "relative_humidity_2m_max": 65.0, "relative_humidity_2m_min": 30.0, "weekday": 0.0},
            {"date": "2025-07-01", "pale_ale": 7.2, "lager": 8.7, "ipa": 5.7, "white": 6.2, "dark": 3.3, "fruit": 3.6, "temperature_2m_mean": 31.5, "weather_code": 2.0, "temperature_2m_max": 35.5, "temperature_2m_min": 28.5, "wind_speed_10m_max": 18.5, "relative_humidity_2m_max": 60.0, "relative_humidity_2m_min": 25.0, "weekday": 1.0},
            {"date": "2025-07-02", "pale_ale": 7.4, "lager": 8.9, "ipa": 5.9, "white": 6.4, "dark": 3.4, "fruit": 3.7, "temperature_2m_mean": 32.0, "weather_code": 3.0, "temperature_2m_max": 36.0, "temperature_2m_min": 29.0, "wind_speed_10m_max": 19.0, "relative_humidity_2m_max": 55.0, "relative_humidity_2m_min": 20.0, "weekday": 2.0},
            {"date": "2025-07-03", "pale_ale": 7.6, "lager": 9.1, "ipa": 6.1, "white": 6.6, "dark": 3.5, "fruit": 3.8, "temperature_2m_mean": 32.5, "weather_code": 1.0, "temperature_2m_max": 36.5, "temperature_2m_min": 29.5, "wind_speed_10m_max": 19.5, "relative_humidity_2m_max": 50.0, "relative_humidity_2m_min": 15.0, "weekday": 3.0},
            {"date": "2025-07-04", "pale_ale": 7.8, "lager": 9.3, "ipa": 6.3, "white": 6.8, "dark": 3.6, "fruit": 3.9, "temperature_2m_mean": 33.0, "weather_code": 2.0, "temperature_2m_max": 37.0, "temperature_2m_min": 30.0, "wind_speed_10m_max": 20.0, "relative_humidity_2m_max": 45.0, "relative_humidity_2m_min": 10.0, "weekday": 4.0}
            ]
            """;
    }
}