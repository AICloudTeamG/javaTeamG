package com.example.javaTeamG.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false); // セッションが存在しない場合は新しく作成しない

        // セッションがない、またはログインIDがない場合は未ログイン
        if (session == null || session.getAttribute("loggedInStaffId") == null) {
            response.sendRedirect("/login"); // ログインページにリダイレクト
            return false; // リクエストの処理を中断
        }

        // 管理者パスへのアクセスチェック
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/admin/")) {
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                // 管理者ではない場合、403 Forbiddenを返すか、アクセス拒否ページへリダイレクト
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. Admin privileges required.");
                // または response.sendRedirect("/access-denied"); // /access-denied ページを別途作成
                return false;
            }
        }
        // StaffsControllerのパスは"/admin/staffs"なので、上記でチェックされます

        return true; // ログイン済みで権限も問題なければ続行
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // レンダリング前にモデルに情報を追加するなどの処理が必要であればここに記述
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // リクエスト処理完了後（ビューレンダリング後）に実行されるクリーンアップ処理など
    }
}