package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.repository.StaffRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Service
public class AuthService {

    private final StaffRepository staffRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // パスワードエンコーダーをインスタンス化
    }

    /**
     * ログイン処理を実行します。
     * @param email 入力されたスタッフのメールアドレス
     * @param rawPassword 入力された平文のパスワード
     * @param session HTTPセッション
     * @return ログイン成功したStaffオブジェクト、または認証失敗時にnull
     */
    public Staff login(String email, String rawPassword, HttpSession session) {
        Staff staff = staffRepository.findByEmail(email);

        // スタッフが存在し、削除されておらず、パスワードが一致する場合
        if (staff != null && !staff.isDeleted() && passwordEncoder.matches(rawPassword, staff.getPassword())) {
            // ログイン成功: セッションにユーザー情報を保存
            session.setAttribute("loggedInStaffId", staff.getId());
            session.setAttribute("loggedInStaffName", staff.getName());
            session.setAttribute("isAdmin", staff.isAdmin());
            return staff;
        }
        return null; // ログイン失敗
    }

    /**
     * ログアウト処理を実行し、セッションを無効化します。
     * @param session HTTPセッション
     */
    public void logout(HttpSession session) {
        session.invalidate(); // セッションを無効化
    }

    /**
     * 現在のセッションが管理者権限を持っているかを確認します。
     * @param session HTTPセッション
     * @return 管理者であればtrue、そうでなければfalse
     */
    public boolean isAdmin(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }

    /**
     * 現在ログイン中のスタッフのIDをセッションから取得します。
     * @param session HTTPセッション
     * @return ログイン中のスタッフID、未ログインであればnull
     */
    public Integer getLoggedInStaffId(HttpSession session) {
        return (Integer) session.getAttribute("loggedInStaffId");
    }

        /**
     * 社員IDに基づいてスタッフ情報を取得します。
     * @param staffId 取得するスタッフのID
     * @return 該当するスタッフのOptional。見つからない場合は空のOptional。
     */
    public Optional<Staff> findStaffById(Integer staffId) {
        return staffRepository.findById(staffId);
    }
}