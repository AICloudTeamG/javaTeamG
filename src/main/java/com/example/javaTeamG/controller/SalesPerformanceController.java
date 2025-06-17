package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.model.SalesPerformance;
import com.example.javaTeamG.model.SalesWeather;
import com.example.javaTeamG.service.AuthService;
import com.example.javaTeamG.service.ProductService;
import com.example.javaTeamG.service.SalesPerformanceService;
import com.example.javaTeamG.service.SalesWeatherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/sales")
public class SalesPerformanceController {

    private final SalesPerformanceService salesPerformanceService;
    private final ProductService productService;
    private final SalesWeatherService salesWeatherService;
    private final AuthService authService;

    public SalesPerformanceController(SalesPerformanceService salesPerformanceService,
                                      ProductService productService,
                                      SalesWeatherService salesWeatherService,
                                      AuthService authService) {
        this.salesPerformanceService = salesPerformanceService;
        this.productService = productService;
        this.salesWeatherService = salesWeatherService;
        this.authService = authService;
    }

    // 全ての認証済みユーザーがアクセス可能だが、管理者とスタッフで一部機能が異なる
    // 役割に応じた詳細なアクセス制御はテンプレート内でThymeleafの条件分岐などで行うか、
    // 必要ならさらに詳細なインターセプター設定を行う。

    /**
     * 販売実績入力フォームを表示します。
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return 販売実績入力ページのビュー名
     */
    @GetMapping("/input")
    public String showSalesInputForm(Model model, HttpSession session) {
        if (authService.getLoggedInStaffId(session) == null) {
            return "redirect:/login"; // 未ログインならログインページへ
        }
        model.addAttribute("salesPerformance", new SalesPerformance());
        model.addAttribute("products", productService.findAllProducts()); // 商品リスト
        model.addAttribute("todayWeather", salesWeatherService.findSalesWeatherByDate(LocalDate.now())); // 今日の天気
        return "sales/input"; // src/main/resources/templates/sales/input.html
    }

    /**
     * 販売実績を登録します。
     * @param salesPerformance 登録する販売実績オブジェクト
     * @param redirectAttributes リダイレクト属性
     * @param session HTTPセッション
     * @return 処理後のリダイレクト先
     */
    @PostMapping("/input")
    public String registerSalesPerformance(@ModelAttribute SalesPerformance salesPerformance,
                                           RedirectAttributes redirectAttributes,
                                           HttpSession session) {
        Integer staffId = authService.getLoggedInStaffId(session);
        if (staffId == null) {
            return "redirect:/login";
        }

        try {
            // SalesPerformanceオブジェクトにProduct, Staff, SalesWeatherを設定
            // 実際はフォームからIDを受け取り、DBから取得してセットする
            salesPerformance.setRecordedByStaff(authService.findStaffById(staffId).orElseThrow()); // ログイン中のスタッフを設定
            // salesPerformance.setProduct(productService.findProductById(salesPerformance.getProduct().getId()).orElseThrow());
            // salesPerformance.setSalesWeather(salesWeatherService.findSalesWeatherById(salesPerformance.getSalesWeather().getId()).orElseThrow());

            salesPerformanceService.saveSalesPerformance(salesPerformance);
            redirectAttributes.addFlashAttribute("message", "販売実績が正常に記録されました。");
        } catch (Exception e) { // IllegalArgumentExceptionなど
            redirectAttributes.addFlashAttribute("errorMessage", "販売実績の記録に失敗しました: " + e.getMessage());
            return "redirect:/sales/input"; // エラーがあれば入力フォームに戻す
        }
        return "redirect:/sales/list";
    }

    /**
     * 販売実績一覧を表示します。
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return 販売実績一覧ページのビュー名
     */
    @GetMapping("/list")
    public String listSalesPerformances(Model model, HttpSession session) {
        if (authService.getLoggedInStaffId(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("salesPerformances", salesPerformanceService.findAllSalesPerformances());
        model.addAttribute("isAdmin", authService.isAdmin(session)); // 管理者フラグをビューに渡す
        return "sales/list"; // src/main/resources/templates/sales/list.html
    }

    /**
     * 販売実績編集フォームを表示します。（管理者用）
     * @param id 編集する販売実績のID
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return 販売実績編集ページのビュー名
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, HttpSession session) {
        if (!authService.isAdmin(session)) { // 管理者のみ
            return "redirect:/access-denied";
        }
        Optional<SalesPerformance> salesPerformance = salesPerformanceService.findSalesPerformanceById(id);
        if (salesPerformance.isPresent()) {
            model.addAttribute("salesPerformance", salesPerformance.get());
            model.addAttribute("products", productService.findAllProducts());
            model.addAttribute("weatherOptions", salesWeatherService.findAllSalesWeather()); // 天気情報の選択肢
            return "sales/edit"; // src/main/resources/templates/sales/edit.html
        } else {
            return "redirect:/sales/list";
        }
    }

    /**
     * 販売実績を更新します。（管理者用）
     * @param id 更新する販売実績のID
     * @param salesPerformance 更新情報を含む販売実績オブジェクト
     * @param redirectAttributes リダイレクト属性
     * @param session HTTPセッション
     * @return 処理後のリダイレクト先
     */
    @PostMapping("/update/{id}")
    public String updateSalesPerformance(@PathVariable Integer id, @ModelAttribute SalesPerformance salesPerformance,
                                         RedirectAttributes redirectAttributes, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        try {
            // IDを設定し、関連エンティティをDBから取得して設定 (フォームの入力に依存)
            salesPerformance.setId(id);
            salesPerformance.setRecordedByStaff(authService.findStaffById(salesPerformance.getRecordedByStaff().getId()).orElseThrow());
            salesPerformance.setProduct(productService.findProductById(salesPerformance.getProduct().getId()).orElseThrow());
            salesPerformance.setSalesWeather(salesWeatherService.findSalesWeatherById(salesPerformance.getSalesWeather().getId()).orElseThrow());

            salesPerformanceService.saveSalesPerformance(salesPerformance); // saveメソッドが更新も兼ねる
            redirectAttributes.addFlashAttribute("message", "販売実績が正常に更新されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "販売実績の更新に失敗しました: " + e.getMessage());
        }
        return "redirect:/sales/list";
    }

    /**
     * 販売実績を論理削除します。（管理者用）
     * @param id 削除する販売実績のID
     * @param redirectAttributes リダイレクト属性
     * @param session HTTPセッション
     * @return 処理後のリダイレクト先
     */
    @PostMapping("/delete/{id}")
    public String deleteSalesPerformance(@PathVariable Integer id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        try {
            salesPerformanceService.deleteSalesPerformance(id);
            redirectAttributes.addFlashAttribute("message", "販売実績が正常に削除されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "販売実績の削除に失敗しました: " + e.getMessage());
        }
        return "redirect:/sales/list";
    }
}