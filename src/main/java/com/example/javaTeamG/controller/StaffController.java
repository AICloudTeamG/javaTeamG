package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.OrderPredictionData;
import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/admin/account")
public class StaffController {

    private final StaffService staffService;

    @Autowired
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    // スタッフ一覧表示
    @GetMapping("/list")
    public String listStaffs(Model model, HttpSession session) {
        List<Staff> staffs = staffService.findAllActiveStaffs();
        model.addAttribute("staffs", staffs);
        model.addAttribute("pageTitle", "アカウント管理");
        
        @SuppressWarnings("unchecked")
        List<OrderPredictionData> forecastWeatherList = (List<OrderPredictionData>) session
                .getAttribute("forecastWeatherList");
        model.addAttribute("forecastWeatherList", forecastWeatherList);

        return "admin/account-management";
    }

    // スタッフ追加処理 (モーダルからPOSTされる)
    @PostMapping("/create")
    public String createStaff(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes,
            @RequestParam boolean isAdmin) {
        try {
            staff.setAdmin(isAdmin);// こうしないと、なぜかfalseになる
            staffService.createStaff(staff);
            redirectAttributes.addFlashAttribute("successMessage", "アカウントが正常に作成されました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // エラー時はモーダルが開いた状態を維持するのが理想ですが、リダイレクトでは難しい
            // ここでは一旦リストにリダイレクトしてメッセージ表示
            return "redirect:/admin/account/list";
        }
        return "redirect:/admin/account/list";
    }

    // スタッフ更新処理 (モーダルからPOSTされる)
    @PostMapping("/edit/{id}")
    public String updateStaff(@PathVariable Integer id, @ModelAttribute Staff staff,
            RedirectAttributes redirectAttributes, @RequestParam boolean isAdmin) {
        try {
            staff.setAdmin(isAdmin);// こうしないと、なぜかfalseになる
            staffService.updateStaff(id, staff);
            redirectAttributes.addFlashAttribute("successMessage", "アカウント情報が更新されました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // エラー時はモーダルが開いた状態を維持するのが理想ですが、リダイレクトでは難しい
            return "redirect:/admin/account/list";
        }
        return "redirect:/admin/account/list";
    }

    // パスワードリセット処理 (モーダルからPOSTされる)
    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Integer id, @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            staffService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "パスワードがリセットされました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/account/list";
    }

    // スタッフ削除処理
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            staffService.deleteStaff(id);
            redirectAttributes.addFlashAttribute("successMessage", "アカウントが正常に削除されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "アカウントの削除に失敗しました。");
        }
        return "redirect:/admin/account/list";
    }
}