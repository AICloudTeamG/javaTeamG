package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.service.AuthService;
import com.example.javaTeamG.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // ★これは必要★
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult; // ★これは必要★

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;
    private final AuthService authService;

    public ProductController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @GetMapping
    public String showProductManagementPage(Model model, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("isAdmin", true);
        List<Product> products = productService.findAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "商品管理");
        return "admin/product-management";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute Product product,
                                BindingResult bindingResult,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "アクセス権限がありません。");
            return "redirect:/access-denied";
        }

        // JANコードの長さチェックはDB制約に任せるが、Bean Validation (例: @NotBlank) のエラーもここで処理
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("入力エラーがあります: ");
            bindingResult.getAllErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            });
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage.toString());
            return "redirect:/admin/products";
        }

        try {
            // Service層での重複チェックが重要になる
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "商品「" + product.getName() + "」が正常に追加されました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "商品の追加に失敗しました: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Integer id,
                                @Valid @ModelAttribute Product product,
                                BindingResult bindingResult,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "アクセス権限がありません。");
            return "redirect:/access-denied";
        }

        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("入力エラーがあります: ");
            bindingResult.getAllErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            });
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage.toString());
            return "redirect:/admin/products";
        }

        try {
            Product updatedProduct = productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("successMessage", "商品「" + updatedProduct.getName() + "」が正常に更新されました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "商品の更新に失敗しました: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "アクセス権限がありません。");
            return "redirect:/access-denied";
        }
        try {
            Optional<Product> productOpt = productService.findProductById(id);
            if (productOpt.isPresent() && !productOpt.get().isDeleted()) {
                productService.deleteProduct(id);
                redirectAttributes.addFlashAttribute("successMessage", "商品「" + productOpt.get().getName() + "」が正常に削除されました。");
            } else if (productOpt.isPresent() && productOpt.get().isDeleted()) {
                redirectAttributes.addFlashAttribute("errorMessage", "商品は既に削除されています。");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "商品の削除に失敗しました。商品が見つかりません。");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "商品の削除に失敗しました: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}