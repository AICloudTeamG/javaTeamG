package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.OrderPredictionData;
import com.example.javaTeamG.model.OrderPredictionDisplayData;
import com.example.javaTeamG.model.SalesWeather; // SalesWeatherをインポート
import com.example.javaTeamG.model.WeatherCode; // WeatherCodeをインポート
import com.example.javaTeamG.service.OrderPredictionService;
import com.example.javaTeamG.service.SalesWeatherService; // SalesWeatherServiceをインポート

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class OrderPredictionController {

    private final OrderPredictionService orderPredictionService;
    private final SalesWeatherService salesWeatherService;

    @Value("${azure.function.prediction.url}")
    private String externalPredictionApiUrl;
    // 予測可能な最大日数 (16日先まで)
    private static final int MAX_PREDICTION_DAYS = 16;
    // 天気予報ポップアップで表示する日数
    private static final int FORECAST_DISPLAY_DAYS = 7;

    @Autowired
    public OrderPredictionController(OrderPredictionService orderPredictionService,
            SalesWeatherService salesWeatherService) {
        this.orderPredictionService = orderPredictionService;
        this.salesWeatherService = salesWeatherService;
    }

    @GetMapping("/admin/prediction")
    public String showPrediction(Model model, HttpSession session) {

        // セッションから既存の予測データを取得
        @SuppressWarnings("unchecked") // キャストの警告を抑制
        List<OrderPredictionData> allPredictions = (List<OrderPredictionData>) session
                .getAttribute("cachedPredictions");

        if (allPredictions == null) {
            // セッションにデータがない場合のみAPIを呼び出す
            try {
                // 本番用
                allPredictions = orderPredictionService.fetchPredictionDataFromExternalApi(externalPredictionApiUrl);
                // ★★★ ここから天気情報をSalesWeatherに反映させるロジック ★★★
                // 当日、1日前、2日前の日付を計算
                LocalDate today = LocalDate.now();
                LocalDate dayBefore1 = today.minusDays(1);
                LocalDate dayBefore2 = today.minusDays(2);

                // 必要な日付の予測データを取得
                // allPredictionsは日付でソートされていると仮定（またはここでソートする）
                allPredictions.sort(Comparator.comparing(OrderPredictionData::getDate));

                List<OrderPredictionData> relevantPredictionDataForWeather = allPredictions.stream()
                        .filter(data -> data.getDate().equals(today) ||
                                data.getDate().equals(dayBefore1) ||
                                data.getDate().equals(dayBefore2))
                        .collect(Collectors.toList());

                // 取得した予測データを使ってSalesWeatherを更新または新規登録
                for (OrderPredictionData predictionData : relevantPredictionDataForWeather) {
                    updateSalesWeatherFromPrediction(predictionData);
                }
                // ★★★ ここまで追加/変更 ★★★

                // 取得したデータをセッションに保存
                session.setAttribute("cachedPredictions", allPredictions);

            } catch (IOException e) {
                System.err.println("Failed to fetch or parse prediction data: " + e.getMessage());
                model.addAttribute("errorMessage", "予測データの取得または処理に失敗しました。");
                return "error";
            }
        }

        if (allPredictions.isEmpty()) {
            model.addAttribute("predictionListForDisplay", new ArrayList<>());
            model.addAttribute("pageTitle", "発注予測");
            return "admin/order-prediction";
        }

        // 日付でソート
        allPredictions.sort(Comparator.comparing(OrderPredictionData::getDate));

        // --- ここから天気予報ポップアップ用のデータ準備 ---
        LocalDate today = LocalDate.now();
        List<OrderPredictionData> forecastWeatherList = allPredictions.stream()
                .filter(data -> !data.getDate().isBefore(today)) // 今日以降のデータのみを対象
                .limit(FORECAST_DISPLAY_DAYS) // 指定された日数（7日間）に制限
                .collect(Collectors.toList());

        session.setAttribute("forecastWeatherList", forecastWeatherList);
        model.addAttribute("forecastWeatherList", forecastWeatherList);
        // --- 天気予報ポップアップ用のデータ準備ここまで ---

        // 直近4回分までの発注日と対象期間の組み合わせを生成
        List<OrderDateRange> orderDateRangesToShow = generateOrderDateRanges(allPredictions); // メソッド名はそのままでロジック変更

        List<OrderPredictionDisplayData> predictionListForDisplay = new ArrayList<>();// jsonで受け取った型をそのまま

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
                    .filter(data -> data.getWeekday() != null
                            && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getPaleAle)
                    .sum();
            double lagerSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null
                            && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getLager)
                    .sum();
            double ipaSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null
                            && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getIpa)
                    .sum();
            double whiteSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null
                            && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getWhite)
                    .sum();
            double darkSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null
                            && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getDark)
                    .sum();
            double fruitSum = relevantPredictions.stream()
                    .filter(data -> data.getWeekday() != null
                            && data.getWeekday().intValue() != DayOfWeek.SUNDAY.getValue())
                    .mapToDouble(OrderPredictionData::getFruit)
                    .sum();

            predictionListForDisplay.add(new OrderPredictionDisplayData(
                    orderDate, startDate, endDate,
                    paleAleSum, lagerSum, ipaSum, whiteSum, darkSum, fruitSum));
        }

        model.addAttribute("predictionListForDisplay", predictionListForDisplay);
        model.addAttribute("pageTitle", "発注予測");

        return "admin/order-prediction";
    }

    // ★追加: OrderPredictionDataからSalesWeatherを更新/作成するプライベートメソッド★
    private void updateSalesWeatherFromPrediction(OrderPredictionData predictionData) {
        // 既存のSalesWeatherデータを日付で検索 (isDeleted=false)
        Optional<SalesWeather> existingSalesWeather = salesWeatherService
                .findSalesWeatherByDate(predictionData.getDate());

        SalesWeather salesWeather;
        if (existingSalesWeather.isPresent()) {
            salesWeather = existingSalesWeather.get();
            System.out.println("Updating existing SalesWeather for date: " + predictionData.getDate());
        } else {
            salesWeather = new SalesWeather();
            salesWeather.setDate(predictionData.getDate());
            salesWeather.setDeleted(false); // 新規作成時はfalse
            System.out.println("Creating new SalesWeather for date: " + predictionData.getDate());
        }

        // OrderPredictionDataの天気情報をSalesWeatherにマッピング
        salesWeather.setTemperatureMax(BigDecimal.valueOf(predictionData.getTemperature2mMax()));
        salesWeather.setTemperatureMin(BigDecimal.valueOf(predictionData.getTemperature2mMin()));
        salesWeather.setTemperatureMean(BigDecimal.valueOf(predictionData.getTemperature2mMean()));
        salesWeather.setHumidityMax(BigDecimal.valueOf(predictionData.getRelativeHumidity2mMax()));
        salesWeather.setHumidityMin(BigDecimal.valueOf(predictionData.getRelativeHumidity2mMin()));
        salesWeather.setWindspeedMax(BigDecimal.valueOf(predictionData.getWindSpeed10mMax()));

        // WeatherCodeの処理
        // OrderPredictionDataのweather_codeはDouble型なので、
        // salesWeatherServiceから適切なWeatherCodeエンティティを取得して設定する必要がある
        // weather_codeがInteger型として使えると仮定し、対応するWeatherCodeを取得する
        // 実際のweather_codeの値と、WeatherCodeテーブルのid/codeのマッピングによってロジックを調整してください
        if (predictionData.getWeatherCode() != null) {
            try {
                // predictionData.getWeather_code().intValue() でDoubleをintに変換し、IDで検索
                WeatherCode wc = salesWeatherService.findWeatherCodeById(predictionData.getWeatherCode().intValue())
                        .orElse(salesWeatherService.getDefaultWeatherCode()); // 見つからなければデフォルト
                salesWeather.setWeatherCode(wc);
            } catch (NumberFormatException e) {
                System.err.println("Invalid weather_code format for date " + predictionData.getDate() + ": "
                        + predictionData.getWeatherCode());
                salesWeather.setWeatherCode(salesWeatherService.getDefaultWeatherCode()); // エラーの場合はデフォルト
            }
        } else {
            salesWeather.setWeatherCode(salesWeatherService.getDefaultWeatherCode()); // nullの場合はデフォルト
        }

        // 保存（新規の場合は登録、既存の場合は更新）
        salesWeatherService.saveSalesWeather(salesWeather);
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

        public LocalDate getOrderDate() {
            return orderDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }
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
                currentEndDate = orderDate.plusWeeks(1); // 翌週の月曜日
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
                boolean hasAllRequiredPredictionData = !finalStartDate.isBefore(firstPredictionDate) &&
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
}