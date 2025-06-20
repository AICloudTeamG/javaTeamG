// src/main/java/com/example/javaTeamG/controller/DataVisualizationController.java
package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.SalesWeatherChartData;
import com.example.javaTeamG.model.SalesWeather;
import com.example.javaTeamG.service.AuthService;
import com.example.javaTeamG.service.DataVisualizationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Collectorsをインポート

import com.example.javaTeamG.model.Product;       // ★ここを追加：Productモデルをインポート
import com.example.javaTeamG.repository.ProductRepository; // ★ここを追加：ProductRepositoryをインポート

@Controller
@RequestMapping("/admin/dashboard")
public class DataVisualizationController {

    private final AuthService authService;
    private final DataVisualizationService dataVisualizationService;
    private final ProductRepository productRepository; // ★ここを追加：ProductRepositoryのフィールド

    // コンストラクタインジェクションにProductRepositoryを追加
    public DataVisualizationController(AuthService authService, 
                                     DataVisualizationService dataVisualizationService,
                                     ProductRepository productRepository) { // ★ここを変更
        this.authService = authService;
        this.dataVisualizationService = dataVisualizationService;
        this.productRepository = productRepository; // ★ここを追加
    }

    /**
     * データ可視化ページを表示します。管理者のみアクセス可能。
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return データ可視化ページのビュー名
     */
    @GetMapping
    public String showDataVisualization(Model model, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("isAdmin", true);

        // ★ここから追加：ビールの銘柄リストをDBから取得し、モデルに追加するロジック
        List<Product> products = productRepository.findByIsDeletedFalse();
        List<String> beerCategories = products.stream()
                                            .map(Product::getName)
                                            .collect(Collectors.toList());
        model.addAttribute("beerCategories", beerCategories); // Thymeleafに渡す

        // 初期表示用のデータを取得（例：今週のデータ）
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        List<SalesWeatherChartData> initialData = dataVisualizationService.getWeeklySalesAndWeatherData(startOfWeek);
        model.addAttribute("initialChartData", initialData);
        model.addAttribute("initialWeekStart", startOfWeek.toString());

        // 今日の天気情報をポップアップ用に取得
        Optional<SalesWeather> todayWeatherOpt = dataVisualizationService.getSalesWeatherForDate(LocalDate.now());
        if (todayWeatherOpt.isPresent()) {
            SalesWeather todayWeather = todayWeatherOpt.get();
            model.addAttribute("todayWeather", todayWeather);
            model.addAttribute("todayWeatherCondition", dataVisualizationService.convertWeatherCodeToEmoji(todayWeather.getWeatherCode().getId()) + " " + todayWeather.getWeatherCode().getDescription());
        } else {
            model.addAttribute("todayWeather", null);
        }

        // ページタイトルも追加（HTML側で ${pageTitle} を使用している場合）
        model.addAttribute("pageTitle", "実績・天気データ可視化");

        return "admin/sales-and-weather";
    }


    @GetMapping("/api/weekly-sales-weather")
    @ResponseBody
    public List<SalesWeatherChartData> getWeeklySalesWeatherChartData(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            HttpSession session) {
        if (!authService.isAdmin(session)) {
            return List.of();
        }
        return dataVisualizationService.getWeeklySalesAndWeatherData(startDate);
    }

    @GetMapping("/api/weather-by-date")
    @ResponseBody
    public Optional<SalesWeather> getWeatherByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpSession session) {
        if (!authService.isAdmin(session)) {
            return Optional.empty();
        }
        return dataVisualizationService.getSalesWeatherForDate(date);
    }
}