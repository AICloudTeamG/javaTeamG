package com.example.javaTeamG.controller;

import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("loginError", "メールアドレスまたはパスワードが正しくありません。");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "ログアウトしました。");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Staff staff = authService.login(email, password, session);
        if (staff != null) {
            return "redirect:/prediction"; // ログイン成功、ダッシュボードへ
        } else {
            return "redirect:/login?error"; // ログイン失敗
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session); // セッションを無効化
        return "redirect:/login?logout"; // ログインページにリダイレクトしてログアウトメッセージを表示
    }


}