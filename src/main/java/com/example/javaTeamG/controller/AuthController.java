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

    /**
     * ログインフォームを表示します。
     * @param error ログイン失敗時のエラーメッセージパラメータ
     * @param logout ログアウト成功時のメッセージパラメータ
     * @param model モデルオブジェクト
     * @return ログインページのビュー名
     */
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
        return "login"; // src/main/resources/templates/login.html を表示
    }

    /**
     * ログイン処理を行います。
     * @param email 入力されたメールアドレス
     * @param password 入力されたパスワード
     * @param session HTTPセッション
     * @param model モデルオブジェクト
     * @return ログイン成功時はダッシュボードへリダイレクト、失敗時はログインページへリダイレクト
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Staff staff = authService.login(email, password, session);
        if (staff != null) {
            return "redirect:/predicition"; // ログイン成功、ダッシュボードへ
        } else {
            return "redirect:/login?error"; // ログイン失敗
        }
    }

    /**
     * ログアウト処理を行います。
     * @param session HTTPセッション
     * @return ログアウト後にログインページへリダイレクト
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session); // セッションを無効化
        return "redirect:/login?logout"; // ログインページにリダイレクトしてログアウトメッセージを表示
    }

    /**
     * ダッシュボードページを表示します。
     * @param session HTTPセッション
     * @param model モデルオブジェクト
     * @return ダッシュボードページのビュー名、未ログインならログインページへリダイレクト
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // インターセプターが認証済みであることを保証しますが、念のため再チェック
        Integer staffId = authService.getLoggedInStaffId(session);
        if (staffId == null) {
            return "redirect:/login"; // 未ログインならログインページへ
        }
        model.addAttribute("loggedInStaffName", session.getAttribute("loggedInStaffName"));
        model.addAttribute("isAdmin", session.getAttribute("isAdmin"));
        return "dashboard"; // ダッシュボードページへ
    }
}