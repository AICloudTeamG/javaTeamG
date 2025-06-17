package com.example.javaTeamG.controller;

import com.example.javaTeamG.service.AuthService;
// import com.example.javaTeamG.service.SalesPerformanceService; // データ取得に利用
// import com.example.javaTeamG.service.SalesWeatherService; // データ取得に利用
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/data") // 管理者専用パス
public class DataVisualizationController {

    // private final SalesPerformanceService salesPerformanceService;
    // private final SalesWeatherService salesWeatherService;
    private final AuthService authService;

    public DataVisualizationController(AuthService authService) { // サービスを注入
        // this.salesPerformanceService = salesPerformanceService;
        // this.salesWeatherService = salesWeatherService;
        this.authService = authService;
    }

    /**
     * データ可視化ページを表示します。管理者のみアクセス可能。
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return データ可視化ページのビュー名
     */
    @GetMapping
    public String showDataVisualization(Model model, HttpSession session) {
        if (!authService.isAdmin(session)) { // 管理者権限チェック
            return "redirect:/access-denied"; // アクセス拒否ページなどへリダイレクト
        }

        // ここでグラフ描画に必要な初期データをモデルに追加することも可能
        return "data/visualization"; // src/main/resources/templates/data/visualization.html
    }

    /**
     * 売上データと天気情報を統合したグラフ用のデータをAPIとして提供します。
     * このデータはフロントエンドのJavaScript（Chart.jsなど）で利用されます。
     * @param session HTTPセッション
     * @return グラフデータ（JSON形式）
     */
    @GetMapping("/api/sales-weather-chart")
    @ResponseBody // JSONレスポンスを返す
    public List<Map<String, Object>> getSalesWeatherChartData(HttpSession session) {
        if (!authService.isAdmin(session)) { // 管理者権限チェック
            // エラーレスポンスを返すか、空のリストを返す
            return List.of();
        }

        // ここに販売実績と天気情報を集計してグラフ用に整形するロジックを実装します。
        // 例: salesPerformanceService と salesWeatherService を組み合わせてデータを取得・加工
        // List<SalesPerformance> sales = salesPerformanceService.findSalesPerformanceByPeriod(...);
        // List<SalesWeather> weather = salesWeatherService.findSalesWeatherByPeriod(...);
        // return dataProcessingService.aggregateSalesAndWeather(sales, weather); // 仮のデータ加工サービス

        // 今はダミーデータを返します。
        return List.of(
            Map.of("date", "2025-06-10", "sales", 120, "tempMean", 25.5),
            Map.of("date", "2025-06-11", "sales", 90, "tempMean", 22.1),
            Map.of("date", "2025-06-12", "sales", 150, "tempMean", 28.0)
        );
    }

    // 商品別売上推移のAPIなども同様に追加可能
    // @GetMapping("/api/product-sales-trend")
    // @ResponseBody
    // public List<Map<String, Object>> getProductSalesTrendData(...) { ... }
}