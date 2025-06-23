package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.SalesInputForm;
import com.example.javaTeamG.model.OrderPredictionData;
import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.model.ProductSalesEntry;
import com.example.javaTeamG.model.SalesPerformance;
import com.example.javaTeamG.model.SalesWeather;
import com.example.javaTeamG.service.AuthService;
import com.example.javaTeamG.service.ProductService;
import com.example.javaTeamG.service.SalesPerformanceService;
import com.example.javaTeamG.service.SalesWeatherService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sales")
public class SalesPerformanceController {

    @Autowired
    private SalesPerformanceService salesPerformanceService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SalesWeatherService salesWeatherService;

    @GetMapping("/input")
    public String showSalesInputForm(
            @RequestParam(value = "date", required = false) LocalDate selectedDate,
            Model model,
            HttpSession session) {

        // 認証情報をセッションから取得
        // AuthServiceのgetLoggedInStaffIdメソッドを使用し、Integer型で受け取る
        Integer employeeId = authService.getLoggedInStaffId(session);
        if (employeeId == null) {
            // ログインしていない場合（またはセッションにIDがない場合）の処理
            return "redirect:/login"; // 適切なログインURLへリダイレクト
        }

        Boolean isAdmin = authService.isAdmin(session);
        model.addAttribute("isAdmin", isAdmin);

        LocalDate targetDate;
        if (isAdmin && selectedDate != null) {
            targetDate = selectedDate;
        } else {
            targetDate = LocalDate.now();
        }
        System.out.println(targetDate);

        SalesInputForm salesInputForm;

        // 初回ロード時または正常なGETリクエスト時
        salesInputForm = new SalesInputForm();
        salesInputForm.setDate(targetDate);
        salesInputForm.setRecorderId(employeeId); // ここでrecorderIdをセット

        // SalesPerformanceServiceのgetSalesPerformanceByDateメソッドを呼び出す
        List<SalesPerformance> existingSales = salesPerformanceService.getSalesPerformanceByDate(targetDate);
        if (!existingSales.isEmpty()) {
            List<ProductSalesEntry> entries = existingSales.stream()
                    .map(sp -> {
                        ProductSalesEntry entry = new ProductSalesEntry();
                        // SalesPerformanceオブジェクトからProduct名とSalesCountを取得
                        entry.setProductName(sp.getProduct().getName());
                        entry.setQuantity(sp.getSalesCount());
                        return entry;
                    })
                    .collect(Collectors.toList());
            salesInputForm.setPerformances(entries);
        } else {
            List<Product> allProducts = productService.findAllProducts();
            List<ProductSalesEntry> initialEntries = allProducts.stream()
                    .map(product -> {
                        ProductSalesEntry entry = new ProductSalesEntry();
                        entry.setProductName(product.getName());
                        entry.setQuantity(0);
                        return entry;
                    })
                    .collect(Collectors.toList());
            salesInputForm.setPerformances(initialEntries);
        }
        // }

        model.addAttribute("salesInputForm", salesInputForm);
        model.addAttribute("pageTitle", isAdmin ? "🍺 販売実績入力（管理者）" : "🍺 販売実績入力（従業員）");
        model.addAttribute("products", productService.findAllProducts());

        // SalesWeatherServiceのfindSalesWeatherByDateメソッドを呼び出す
        Optional<SalesWeather> weatherOptional = salesWeatherService.findSalesWeatherByDate(LocalDate.now());
        model.addAttribute("todayWeather", weatherOptional.orElse(null));

        @SuppressWarnings("unchecked")
        List<OrderPredictionData> forecastWeatherList = (List<OrderPredictionData>) session
                .getAttribute("forecastWeatherList");
        model.addAttribute("forecastWeatherList", forecastWeatherList);

        return "sales-input";
    }

    @PostMapping("/input")
    public String processSalesInput(@Valid @ModelAttribute("salesInputForm") SalesInputForm salesInputForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        // 認証情報をセッションから取得
        Integer employeeId = authService.getLoggedInStaffId(session);
        if (employeeId == null) {
            return "redirect:/login"; // 適切なログインURLへリダイレクト
        }
        salesInputForm.setRecorderId(employeeId); // Integer型をセット

        Boolean isAdmin = authService.isAdmin(session);

        if (!isAdmin && !salesInputForm.getDate().isEqual(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "従業員は当日の売上のみ登録できます。");
            redirectAttributes.addFlashAttribute("salesInputForm", salesInputForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.salesInputForm",
                    bindingResult);
            return "redirect:/sales/input";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "入力に不備があります。もう一度確認してください。");
            redirectAttributes.addFlashAttribute("salesInputForm", salesInputForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.salesInputForm",
                    bindingResult);
            return "redirect:/sales/input";
        }

        try {
            salesPerformanceService.saveOrUpdateMultipleSalesPerformance(
                    salesInputForm.getDate(),
                    salesInputForm.getRecorderId(),
                    salesInputForm.getPerformances());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "実績の保存中にエラーが発生しました: " + e.getMessage());
            redirectAttributes.addFlashAttribute("salesInputForm", salesInputForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.salesInputForm",
                    bindingResult);
            return "redirect:/sales/input";
        }

        redirectAttributes.addFlashAttribute("successMessage", "販売実績を登録しました。");
        return "redirect:/sales/input";
    }
}