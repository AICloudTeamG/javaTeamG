package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.model.ProductSalesEntry;
import com.example.javaTeamG.model.SalesInputForm;
import com.example.javaTeamG.model.SalesPerformance;
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
import java.util.ArrayList;
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

    /**
     * 販売実績入力フォームを表示します。
     * 管理者は過去の日付も選択・修正可能。従業員は当日固定。
     *
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @param selectedDate 管理者が選択した日付（修正時など）
     * @return 販売実績入力ページのビュー名
     */
    @GetMapping("/input")
    public String showSalesInputForm(
            @RequestParam(value = "date", required = false) LocalDate selectedDate,
            Model model,
            HttpSession session) {

        Integer staffId = authService.getLoggedInStaffId(session);
        if (staffId == null) {
            return "redirect:/login"; // 未ログインならログインページへ
        }

        Boolean isAdmin = authService.isAdmin(session);
        model.addAttribute("isAdmin", isAdmin);

        LocalDate targetDate = (selectedDate != null && isAdmin) ? selectedDate : LocalDate.now();
        model.addAttribute("pageTitle", isAdmin ? "🍺 販売実績入力（管理者）" : "🍺 販売実績入力（従業員）");

        SalesInputForm salesInputForm;
        if (model.containsAttribute("salesInputForm")) {
            // リダイレクトされた場合（エラーなど）はFlash Attributeからフォームデータを取得
            salesInputForm = (SalesInputForm) model.asMap().get("salesInputForm");
        } else {
            salesInputForm = new SalesInputForm();
            salesInputForm.setDate(targetDate);
            salesInputForm.setRecorderId(staffId);

            // 選択された日付の既存データをロード
            List<SalesPerformance> existingPerformances = salesPerformanceService.getSalesPerformanceByDate(targetDate);
            
            // 全商品リストを取得し、既存データをプリセット
            List<Product> allProducts = productService.findAllProducts();
            List<ProductSalesEntry> productSalesEntries = new ArrayList<>();

            for (Product product : allProducts) {
                ProductSalesEntry entry = new ProductSalesEntry();
                entry.setProductName(product.getName());
                // 既存データから数量を見つける
                Optional<SalesPerformance> existing = existingPerformances.stream()
                        .filter(sp -> sp.getProduct().getId().equals(product.getId()))
                        .findFirst();
                entry.setQuantity(existing.map(SalesPerformance::getSalesCount).orElse(0)); // 既存があればその数量、なければ0
                productSalesEntries.add(entry);
            }
            salesInputForm.setPerformances(productSalesEntries);
        }
        
        model.addAttribute("salesInputForm", salesInputForm);
        model.addAttribute("products", productService.findAllProducts()); // 商品リスト (念のため、テンプレートで直接使用する可能性も考慮)
        
        // SalesWeatherService.findSalesWeatherByDateがOptionalを返すことを前提に修正
        model.addAttribute("todayWeather", salesWeatherService.findSalesWeatherByDate(LocalDate.now()).orElse(null)); // 今日の天気
        
        return "sales-input"; // src/main/resources/templates/sales-input.html
    }

    /**
     * 販売実績を登録または更新します。
     * @param salesInputForm 登録/更新する販売実績のフォームデータ
     * @param redirectAttributes リダイレクト属性
     * @param session HTTPセッション
     * @return 処理後のリダイレクト先
     */
    @PostMapping("/input")
    public String processSalesInput(@ModelAttribute("salesInputForm") SalesInputForm salesInputForm,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {

        Integer loggedInStaffId = authService.getLoggedInStaffId(session);
        if (loggedInStaffId == null) {
            return "redirect:/login";
        }

        Boolean isAdmin = authService.isAdmin(session);

        // 従業員の場合、日付が本日であることを確認
        if (!isAdmin && !salesInputForm.getDate().isEqual(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "従業員は当日の売上のみ登録できます。");
            redirectAttributes.addFlashAttribute("salesInputForm", salesInputForm); // エラー時にフォームデータを保持
            return "redirect:/sales-input";
        }

        try {
            salesPerformanceService.saveOrUpdateMultipleSalesPerformance(
                salesInputForm.getDate(),
                loggedInStaffId,
                salesInputForm.getPerformances()
            );
            redirectAttributes.addFlashAttribute("successMessage", "販売実績が正常に記録されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "販売実績の記録に失敗しました: " + e.getMessage());
            redirectAttributes.addFlashAttribute("salesInputForm", salesInputForm); // エラー時にフォームデータを保持
            return "redirect:/sales-input";
        }
        // 成功後も入力フォームに戻る（当日または選択日のデータが表示される）
        return "redirect:/sales-input?date=" + salesInputForm.getDate().toString(); 
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
        // 論理削除されていないデータのみ表示するべき (サービス層でフィルタリング)
        model.addAttribute("salesPerformances", salesPerformanceService.findAllActiveSalesPerformances()); 
        model.addAttribute("isAdmin", authService.isAdmin(session));
        return "sales/list"; 
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
        if (!authService.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        Optional<SalesPerformance> salesPerformance = salesPerformanceService.findSalesPerformanceById(id);
        if (salesPerformance.isPresent()) {
            model.addAttribute("salesPerformance", salesPerformance.get());
            model.addAttribute("products", productService.findAllProducts());
            model.addAttribute("weatherOptions", salesWeatherService.findAllSalesWeather()); // 天気情報の選択肢
            return "sales/edit";
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
            salesPerformance.setId(id);
            // 関連エンティティをDBから取得して設定 (フォームの入力に依存)
            salesPerformance.setRecordedByStaff(authService.findStaffById(salesPerformance.getRecordedByStaff().getId())
                                                              .orElseThrow(() -> new IllegalArgumentException("記録者スタッフが見つかりません")));
            salesPerformance.setProduct(productService.findProductById(salesPerformance.getProduct().getId())
                                                          .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません")));
            // SalesWeatherService.findSalesWeatherByIdがOptionalを返すことを前提に修正
            salesPerformance.setSalesWeather(salesWeatherService.findSalesWeatherById(salesPerformance.getSalesWeather().getId())
                                                                  .orElseThrow(() -> new IllegalArgumentException("天気情報が見つかりません")));
            // createdAtは新規作成時のみセットされるため、更新時は既存の値を使うか、DBから取得してセット
            Optional<SalesPerformance> existing = salesPerformanceService.findSalesPerformanceById(id);
            existing.ifPresent(sp -> salesPerformance.setCreatedAt(sp.getCreatedAt()));

            salesPerformanceService.saveSalesPerformance(salesPerformance); // saveメソッドが更新も兼ねる
            redirectAttributes.addFlashAttribute("message", "販売実績が正常に更新されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "販売実績の更新に失敗しました: " + e.getMessage());
            // エラー時は編集フォームに戻すため、IDを保持してリダイレクト
            return "redirect:/sales/edit/" + id; 
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
            salesPerformanceService.deleteSalesPerformance(id); // サービス層で論理削除フラグを立てる
            redirectAttributes.addFlashAttribute("message", "販売実績が正常に削除されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "販売実績の削除に失敗しました: " + e.getMessage());
        }
        return "redirect:/sales/list";
    }
}