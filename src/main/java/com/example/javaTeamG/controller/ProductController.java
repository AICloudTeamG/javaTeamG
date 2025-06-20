// src/main/java/com/example/javaTeamG/controller/ProductController.java
package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.service.AuthService;
import com.example.javaTeamG.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
        model.addAttribute("product", new Product());
        return "admin/product-management";
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addProductApi(@RequestBody Product product, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "アクセスが拒否されました。"));
        }
        try {
            Product newProduct = productService.createProduct(product);
            return ResponseEntity.ok(Map.of("message", "商品が正常に追加されました。", "product", newProduct));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "商品の追加に失敗しました: " + e.getMessage()));
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getProductApi(@PathVariable Integer id, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "アクセスが拒否されました。"));
        }
        Optional<Product> productOpt = productService.findProductById(id);
        if (productOpt.isPresent()) {
            return ResponseEntity.ok(productOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "ID " + id + " の商品が見つかりません。"));
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProductApi(@PathVariable Integer id, @RequestBody Product product, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "アクセスが拒否されました。"));
        }
        try {
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(Map.of("message", "商品が正常に更新されました。", "product", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "商品の更新に失敗しました: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProductApi(@PathVariable Integer id, HttpSession session) {
        if (!authService.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "アクセスが拒否されました。"));
        }
        try {
            Optional<Product> productOpt = productService.findProductById(id);
            if (productOpt.isPresent() && !productOpt.get().isDeleted()) {
                productService.deleteProduct(id);
                return ResponseEntity.ok(Map.of("message", "商品が正常に削除されました。"));
            } else if (productOpt.isPresent() && productOpt.get().isDeleted()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "商品は既に削除されています。"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "商品の削除に失敗しました。商品が見つかりません。"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "商品の削除に失敗しました: " + e.getMessage()));
        }
    }
}