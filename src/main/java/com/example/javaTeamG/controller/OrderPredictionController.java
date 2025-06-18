package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.OrderPredictionData; 
import com.example.javaTeamG.service.OrderPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class OrderPredictionController {

    private final OrderPredictionService orderPredictionService; 

    // 外部APIのURL (TODO: 実際のURLに置き換える)
    private static final String EXTERNAL_PREDICTION_API_URL = "/dummy-order-prediction-data"; // ダミーAPIのパスも変更

    @Autowired
    public OrderPredictionController(OrderPredictionService orderPredictionService) { 
        this.orderPredictionService = orderPredictionService;
    }

    @GetMapping("/prediction")
    // @PreAuthorize("hasRole('ADMIN')")
    public String showWeeklyPrediction(Model model,
                                       @RequestParam(name = "selectedOrderDate", required = false) String selectedOrderDateStr) {
        List<OrderPredictionData> allPredictions;
        try {
            String dummyJsonData = getDummyPredictionDataString();
            allPredictions = orderPredictionService.parsePredictionData(dummyJsonData);
            // TODO: 実際の外部APIからデータを取得する場合は以下の行を使用
            // allPredictions = orderPredictionService.fetchPredictionDataFromExternalApi(EXTERNAL_PREDICTION_API_URL);

        } catch (IOException e) {
            System.err.println("Failed to fetch or parse prediction data: " + e.getMessage());
            model.addAttribute("errorMessage", "予測データの取得または処理に失敗しました。");
            return "error";
        }

        if (allPredictions.isEmpty()) {
            model.addAttribute("predictionList", new ArrayList<>());
            model.addAttribute("nextOrderDate", LocalDate.now());
            model.addAttribute("orderDateRanges", new ArrayList<>());
            model.addAttribute("selectedOrderDate", "");
            model.addAttribute("pageTitle", "発注予測");
            return "order-prediction"; // テンプレートパスも変更
        }

        allPredictions.sort(Comparator.comparing(OrderPredictionData::getDate));

        List<OrderDateRange> orderDateRanges = generateOrderDateRanges(allPredictions);
        model.addAttribute("orderDateRanges", orderDateRanges);

        LocalDate currentOrderDate;

        if (selectedOrderDateStr != null && !selectedOrderDateStr.isEmpty()) {
            currentOrderDate = LocalDate.parse(selectedOrderDateStr);
        } else {
            currentOrderDate = determineDefaultOrderDate(LocalDate.now());
        }

        model.addAttribute("selectedOrderDate", currentOrderDate.toString());
        model.addAttribute("nextOrderDate", currentOrderDate);

        List<OrderPredictionData> displayPredictions = new ArrayList<>();

        Optional<OrderDateRange> selectedRangeOpt = orderDateRanges.stream()
                .filter(r -> r.getOrderDate().isEqual(currentOrderDate))
                .findFirst();

        if (selectedRangeOpt.isPresent()) {
            OrderDateRange range = selectedRangeOpt.get();
            LocalDate startDate = range.getStartDate();
            LocalDate endDate = range.getEndDate();

            List<OrderPredictionData> relevantPredictions = allPredictions.stream()
                    .filter(data -> !data.getDate().isBefore(startDate) && !data.getDate().isAfter(endDate))
                    .collect(Collectors.toList());

            displayPredictions.addAll(relevantPredictions);
        } else {
            System.err.println("No matching order date range found for: " + currentOrderDate);
        }

        model.addAttribute("predictionList", displayPredictions);
        model.addAttribute("pageTitle", "発注予測");

        return "order-prediction"; // テンプレートパスも変更
    }

    public static class OrderDateRange {
        private LocalDate orderDate;
        private LocalDate startDate;
        private LocalDate endDate;
        private String displayString;

        public OrderDateRange(LocalDate orderDate, LocalDate startDate, LocalDate endDate) {
            this.orderDate = orderDate;
            this.startDate = startDate;
            this.endDate = endDate;
            this.displayString = String.format("%s〜%s",
                    startDate.format(DateTimeFormatter.ofPattern("M/d")),
                    endDate.format(DateTimeFormatter.ofPattern("M/d"))
            );
        }

        public LocalDate getOrderDate() { return orderDate; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getDisplayString() { return displayString; }
    }

    private List<OrderDateRange> generateOrderDateRanges(List<OrderPredictionData> allPredictions) {
        List<OrderDateRange> ranges = new ArrayList<>();
        if (allPredictions.isEmpty()) {
            return ranges;
        }

        LocalDate firstPredictionDate = allPredictions.get(0).getDate();
        LocalDate lastPredictionDate = allPredictions.get(allPredictions.size() - 1).getDate();

        LocalDate dateIterator = firstPredictionDate;
        while (!dateIterator.isAfter(lastPredictionDate.plusWeeks(1))) {
            if (dateIterator.getDayOfWeek() == DayOfWeek.MONDAY) {
                LocalDate startDate = dateIterator.plusDays(1); // 火曜日
                LocalDate endDate = dateIterator.plusWeeks(1); // 翌週の月曜日
                if (isWithinPredictionRange(startDate, endDate, allPredictions)) {
                    ranges.add(new OrderDateRange(dateIterator, startDate, endDate));
                }
            } else if (dateIterator.getDayOfWeek() == DayOfWeek.THURSDAY) {
                LocalDate startDate = dateIterator.plusDays(1); // 金曜日
                LocalDate endDate = dateIterator.plusWeeks(1).with(DayOfWeek.MONDAY); // 翌週の月曜日
                if (isWithinPredictionRange(startDate, endDate, allPredictions)) {
                    ranges.add(new OrderDateRange(dateIterator, startDate, endDate));
                }
            } else if (dateIterator.getDayOfWeek() == DayOfWeek.FRIDAY) {
                LocalDate startDate = dateIterator; // 金曜日
                LocalDate endDate = dateIterator.plusDays(3).with(DayOfWeek.MONDAY); // 翌週の月曜日
                if (isWithinPredictionRange(startDate, endDate, allPredictions)) {
                    ranges.add(new OrderDateRange(dateIterator, startDate, endDate));
                }
            }
            dateIterator = dateIterator.plusDays(1);
        }

        return ranges.stream()
                .collect(Collectors.toMap(OrderDateRange::getOrderDate, r -> r, (existing, replacement) -> existing))
                .values().stream()
                .sorted(Comparator.comparing(OrderDateRange::getOrderDate))
                .collect(Collectors.toList());
    }

    private boolean isWithinPredictionRange(LocalDate startDate, LocalDate endDate, List<OrderPredictionData> allPredictions) {
        if (allPredictions.isEmpty()) return false;
        LocalDate minPredictionDate = allPredictions.get(0).getDate();
        LocalDate maxPredictionDate = allPredictions.get(allPredictions.size() - 1).getDate();

        return (!startDate.isAfter(maxPredictionDate) || !endDate.isBefore(minPredictionDate))
                && !startDate.isAfter(endDate);
    }

    private LocalDate determineDefaultOrderDate(LocalDate currentDate) {
        LocalDate defaultDate = currentDate;

        if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            return currentDate;
        } else if (currentDate.getDayOfWeek() == DayOfWeek.THURSDAY) {
            return currentDate;
        } else if (currentDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return currentDate;
        } else {
            LocalDate nextOrderCandidate = currentDate;
            while (true) {
                if (nextOrderCandidate.getDayOfWeek() == DayOfWeek.MONDAY ||
                    nextOrderCandidate.getDayOfWeek() == DayOfWeek.THURSDAY ||
                    nextOrderCandidate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                    return nextOrderCandidate;
                }
                nextOrderCandidate = nextOrderCandidate.plusDays(1);
            }
        }
    }

    public String getWeatherDescription(Double code) {
        if (code == null) return "不明";
        int intCode = code.intValue();
        switch(intCode) {
            case 0: return "快晴";
            case 1:
            case 2: return "晴れ";
            case 3: return "曇り";
            case 45:
            case 48: return "霧";
            case 51:
            case 53:
            case 55: return "霧雨";
            case 56:
            case 57: return "着氷性霧雨";
            case 61: return "小雨";
            case 63: return "雨";
            case 65: return "大雨";
            case 66:
            case 67: return "着氷性の雨";
            case 71: return "小雪";
            case 73: return "雪";
            case 75: return "大雪";
            case 77: return "雪の粒";
            case 80: return "小雨/にわか雨";
            case 81: return "雨/にわか雨";
            case 82: return "豪雨/にわか雨";
            case 85: return "小雪/にわか雪";
            case 86: return "大雪/にわか雪";
            case 95: return "雷雨";
            case 96:
            case 99: return "ひょうを伴う雷雨";
            default: return "不明";
        }
    }

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

    private String getDummyPredictionDataString() {
        return """
            [{"date": "2025-06-18", "pale_ale": 5.59, "lager": 8.06, "ipa": 4.82, "white": 5.01, "dark": 3.11, "fruit": 3.72, "temperature_2m_mean": 27.5, "weather_code": 1.0, "temperature_2m_max": 33.0, "temperature_2m_min": 23.0, "wind_speed_10m_max": 7.1, "relative_humidity_2m_max": 93.0, "relative_humidity_2m_min": 50.0, "weekday": 2.0}, {"date": "2025-06-19", "pale_ale": 6.5, "lager": 8.55, "ipa": 5.25, "white": 5.32, "dark": 3.33, "fruit": 4.14, "temperature_2m_mean": 26.4, "weather_code": 3.0, "temperature_2m_max": 30.8, "temperature_2m_min": 23.4, "wind_speed_10m_max": 7.1, "relative_humidity_2m_max": 98.0, "relative_humidity_2m_min": 60.0, "weekday": 3.0}, {"date": "2025-06-20", "pale_ale": 10.65, "lager": 11.23, "ipa": 7.49, "white": 6.7, "dark": 4.73, "fruit": 4.35, "temperature_2m_mean": 25.5, "weather_code": 2.0, "temperature_2m_max": 29.6, "temperature_2m_min": 21.9, "wind_speed_10m_max": 7.4, "relative_humidity_2m_max": 97.0, "relative_humidity_2m_min": 65.0, "weekday": 4.0}, {"date": "2025-06-21", "pale_ale": 5.76, "lager": 7.38, "ipa": 4.58, "white": 5.13, "dark": 2.67, "fruit": 4.03, "temperature_2m_mean": 25.4, "weather_code": 3.0, "temperature_2m_max": 29.4, "temperature_2m_min": 22.2, "wind_speed_10m_max": 18.7, "relative_humidity_2m_max": 95.0, "relative_humidity_2m_min": 55.0, "weekday": 5.0}, {"date": "2025-06-22", "pale_ale": 6.21, "lager": 7.32, "ipa": 4.55, "white": 4.46, "dark": 2.85, "fruit": 3.89, "temperature_2m_mean": 26.3, "weather_code": 3.0, "temperature_2m_max": 30.9, "temperature_2m_min": 22.5, "wind_speed_10m_max": 24.3, "relative_humidity_2m_max": 88.0, "relative_humidity_2m_min": 53.0, "weekday": 6.0}, {"date": "2025-06-23", "pale_ale": 5.7, "lager": 6.05, "ipa": 3.72, "white": 3.49, "dark": 2.47, "fruit": 2.62, "temperature_2m_mean": 24.8, "weather_code": 61.0, "temperature_2m_max": 26.6, "temperature_2m_min": 23.7, "wind_speed_10m_max": 14.8, "relative_humidity_2m_max": 89.0, "relative_humidity_2m_min": 71.0, "weekday": 0.0}, {"date": "2025-06-24", "pale_ale": 5.3, "lager": 6.68, "ipa": 4.23, "white": 4.37, "dark": 2.79, "fruit": 3.09, "temperature_2m_mean": 26.4, "weather_code": 3.0, "temperature_2m_max": 30.7, "temperature_2m_min": 23.1, "wind_speed_10m_max": 17.7, "relative_humidity_2m_max": 92.0, "relative_humidity_2m_min": 59.0, "weekday": 1.0}, {"date": "2025-06-25", "pale_ale": 5.47, "lager": 6.49, "ipa": 3.44, "white": 4.37, "dark": 2.74, "fruit": 3.06, "temperature_2m_mean": 26.7, "weather_code": 3.0, "temperature_2m_max": 29.5, "temperature_2m_min": 24.3, "wind_speed_10m_max": 16.5, "relative_humidity_2m_max": 92.0, "relative_humidity_2m_min": 64.0, "weekday": 2.0}, {"date": "2025-06-26", "pale_ale": 5.27, "lager": 7.15, "ipa": 3.79, "white": 4.59, "dark": 2.39, "fruit": 2.66, "temperature_2m_mean": 27.9, "weather_code": 51.0, "temperature_2m_max": 31.9, "temperature_2m_min": 24.7, "wind_speed_10m_max": 21.2, "relative_humidity_2m_max": 77.0, "relative_humidity_2m_min": 47.0, "weekday": 3.0}]
            """;
    }
}