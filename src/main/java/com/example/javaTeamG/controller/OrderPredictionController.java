package com.example.javaTeamG.controller;

import com.example.javaTeamG.service.AuthService;
import com.example.javaTeamG.service.OrderPredictionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@RequestMapping("/admin/prediction") // 管理者専用パス
public class OrderPredictionController {

    private final OrderPredictionService orderPredictionService;
    private final AuthService authService;

    public OrderPredictionController(OrderPredictionService orderPredictionService, AuthService authService) {
        this.orderPredictionService = orderPredictionService;
        this.authService = authService;
    }

    /**
     * 発注予測ページを表示します。管理者のみアクセス可能。
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return 発注予測ページのビュー名
     */
    @GetMapping
    public String showOrderPrediction(Model model, HttpSession session) {
        if (!authService.isAdmin(session)) { // 管理者権限チェック
            return "redirect:/access-denied"; // アクセス拒否ページなどへリダイレクト
        }

        // 今週の月曜日を取得
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 発注予測データを取得（仮のデータ）
        // List<OrderPrediction> predictions = orderPredictionService.getWeeklyOrderPrediction(startOfWeek);
        // model.addAttribute("predictions", predictions);

        model.addAttribute("weekStartDate", startOfWeek);
        // model.addAttribute("weatherForecast", ...); // 天気予報データもここに統合可能

        return "prediction/weekly"; // src/main/resources/templates/prediction/weekly.html
    }

    // 必要に応じて、予測データだけを返すRESTful APIも追加可能
    // @GetMapping("/api")
    // @ResponseBody
    // public List<OrderPrediction> getPredictionApi(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
    //     return orderPredictionService.getWeeklyOrderPrediction(startDate);
    // }
}
