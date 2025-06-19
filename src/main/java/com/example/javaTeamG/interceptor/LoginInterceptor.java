package com.example.javaTeamG.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.javaTeamG.model.Staff;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false); // セッションが存在しない場合は新しく作成しない

        // ログインページへのアクセスは許可
        if (request.getRequestURI().equals("/login")) {
            return true;
        }

        // セッションがない、またはセッションにユーザー情報がない場合はログインページへリダイレクト
        if (session == null || session.getAttribute("loggedInStaff") == null) {
            response.sendRedirect("/login");
            return false; // 処理を中断
        }

        // ここから管理者ページへのアクセス制限のロジック
        // 例: `/admin/` で始まるパスは isAdmin が true のユーザーのみアクセス可能
        if (request.getRequestURI().startsWith("/admin/")) {
            Staff loggedInStaff = (Staff) session.getAttribute("loggedInStaff"); // セッションからStaffオブジェクトを取得

            if (loggedInStaff == null || !loggedInStaff.isAdmin()) {
                // 管理者ではない場合、アクセス拒否またはエラーページへリダイレクト
                response.sendRedirect("/access-denied"); // 例: アクセス拒否ページ
                return false;
            }
        }

        return true; // 処理を続行
    }

}