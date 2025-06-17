package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.service.AuthService;
import com.example.javaTeamG.service.StaffService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/staffs") // 管理者専用パス
public class StaffController {

    private final StaffService staffService;
    private final AuthService authService;

    public StaffController(StaffService staffService, AuthService authService) {
        this.staffService = staffService;
        this.authService = authService;
    }

    // インターセプターで管理者権限をチェックしているため、ここでは単純にリダイレクトのみ
    private String requireAdmin(HttpSession session) {
        // LoginInterceptorが権限チェックを行うため、ここでは念のための確認
        // 実際にはインターセプターで処理され、ここには到達しないか、既にエラーレスポンスが返されている
        if (!authService.isAdmin(session)) {
            // 例: より詳細なエラーページやメッセージ
            return "redirect:/access-denied"; // アクセス拒否ページなどへリダイレクト
        }
        return null; // 問題なければnullを返す
    }

    /**
     * 全スタッフの一覧を表示します。管理者のみアクセス可能。
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return スタッフ一覧ページのビュー名
     */
    @GetMapping
    public String listStaffs(Model model, HttpSession session) {
        String adminCheck = requireAdmin(session);
        if (adminCheck != null) return adminCheck;

        model.addAttribute("staffs", staffService.findAllStaffs());
        return "staffs/list"; // src/main/resources/templates/staffs/list.html
    }

    /**
     * 新規スタッフ作成フォームを表示します。管理者のみアクセス可能。
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return 新規スタッフ作成ページのビュー名
     */
    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        String adminCheck = requireAdmin(session);
        if (adminCheck != null) return adminCheck;

        model.addAttribute("staff", new Staff());
        return "staffs/create"; // src/main/resources/templates/staffs/create.html
    }

    /**
     * 新規スタッフを登録します。管理者のみアクセス可能。
     * @param staff 登録するスタッフ情報
     * @param redirectAttributes リダイレクト属性
     * @param session HTTPセッション
     * @return 処理後のリダイレクト先
     */
    @PostMapping
    public String createStaff(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes, HttpSession session) {
        String adminCheck = requireAdmin(session);
        if (adminCheck != null) return adminCheck;

        try {
            staffService.createStaff(staff);
            redirectAttributes.addFlashAttribute("message", "スタッフが正常に作成されました。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/staffs/new";
        }
        return "redirect:/admin/staffs";
    }

    /**
     * スタッフ編集フォームを表示します。管理者のみアクセス可能。
     * @param id 編集するスタッフのID
     * @param model モデルオブジェクト
     * @param session HTTPセッション
     * @return スタッフ編集ページのビュー名
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, HttpSession session) {
        String adminCheck = requireAdmin(session);
        if (adminCheck != null) return adminCheck;

        Optional<Staff> staff = staffService.findStaffById(id);
        if (staff.isPresent()) {
            model.addAttribute("staff", staff.get());
            return "staffs/edit"; // src/main/resources/templates/staffs/edit.html
        } else {
            return "redirect:/admin/staffs"; // 見つからない場合は一覧へ
        }
    }

    /**
     * スタッフ情報を更新します。管理者のみアクセス可能。
     * @param id 更新するスタッフのID
     * @param staff 更新情報を含むスタッフオブジェクト
     * @param redirectAttributes リダイレクト属性
     * @param session HTTPセッション
     * @return 処理後のリダイレクト先
     */
    @PostMapping("/update/{id}")
    public String updateStaff(@PathVariable Integer id, @ModelAttribute Staff staff, RedirectAttributes redirectAttributes, HttpSession session) {
        String adminCheck = requireAdmin(session);
        if (adminCheck != null) return adminCheck;

        try {
            staffService.updateStaff(id, staff);
            redirectAttributes.addFlashAttribute("message", "スタッフ情報が正常に更新されました。");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/staffs";
    }

    /**
     * スタッフアカウントを論理削除します。管理者のみアクセス可能。
     * @param id 削除するスタッフのID
     * @param redirectAttributes リダイレクト属性
     * @param session HTTPセッション
     * @return 処理後のリダイレクト先
     */
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirectAttributes, HttpSession session) {
        String adminCheck = requireAdmin(session);
        if (adminCheck != null) return adminCheck;

        try {
            staffService.deleteStaff(id);
            redirectAttributes.addFlashAttribute("message", "スタッフが正常に削除されました。");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/staffs";
    }
}