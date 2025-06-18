package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.repository.StaffRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCryptPasswordEncoderを使用
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.Optional; // Optionalをインポート

@Service
public class AuthService {

    private final StaffRepository staffRepository;
    private final BCryptPasswordEncoder passwordEncoder; // BCryptPasswordEncoderをDIで受け取るように修正

    // コンストラクタでBCryptPasswordEncoderも注入するように変更
    public AuthService(StaffRepository staffRepository, BCryptPasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder; // 注入されたインスタンスを使用
    }

    /**
     * ログイン処理を実行します。
     * @param email 入力されたスタッフのメールアドレス
     * @param rawPassword 入力された平文のパスワード
     * @param session HTTPセッション
     * @return ログイン成功したStaffオブジェクト、または認証失敗時にnull
     */
    public Staff login(String email, String rawPassword, HttpSession session) {
        Optional<Staff> staffOptional = staffRepository.findByEmail(email); // Optionalを返すように変更

        // OptionalからStaffオブジェクトを取得し、存在チェック
        if (staffOptional.isPresent()) {
            Staff staff = staffOptional.get();

            // スタッフが削除されておらず、パスワードが一致する場合
            if (!staff.isDeleted() && passwordEncoder.matches(rawPassword, staff.getPassword())) {
                // ログイン成功: セッションにユーザー情報を保存 (LoginInterceptorに合わせてStaffオブジェクトを保存)
                session.setAttribute("loggedInStaff", staff); // Staffオブジェクトを直接保存
                // 個別の属性も必要であれば残す (isAdminはStaffオブジェクトから取得可能なので不要かも)
                session.setAttribute("loggedInStaffId", staff.getId());
                session.setAttribute("loggedInStaffName", staff.getName());
                session.setAttribute("isAdmin", staff.isAdmin()); // isAdminもStaffオブジェクトから取得できるが、利便性のために残す
                return staff;
            }
        }
        return null; // ログイン失敗 (スタッフが存在しない、削除されている、またはパスワード不一致)
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
        // Staffオブジェクトがセッションにあるか確認し、そこからisAdminを取得
        Staff loggedInStaff = (Staff) session.getAttribute("loggedInStaff");
        return loggedInStaff != null && loggedInStaff.isAdmin();
    }

    /**
     * 現在ログイン中のスタッフのIDをセッションから取得します。
     * @param session HTTPセッション
     * @return ログイン中のスタッフID、未ログインであればnull
     */
    public Integer getLoggedInStaffId(HttpSession session) {
        // StaffオブジェクトからIDを取得することも可能
        Staff loggedInStaff = (Staff) session.getAttribute("loggedInStaff");
        return (loggedInStaff != null) ? loggedInStaff.getId() : null;
    }

    // このメソッドはStaffServiceに移譲するか、AuthServiceの目的と照らし合わせて残すかを検討
    // 認証情報以外のスタッフの詳細情報はStaffServiceで扱うのが一般的
    /**
     * 社員IDに基づいてスタッフ情報を取得します。
     * @param staffId 取得するスタッフのID
     * @return 該当するスタッフのOptional。見つからない場合は空のOptional。
     */
    public Optional<Staff> findStaffById(Integer staffId) {
        return staffRepository.findById(staffId);
    }
}