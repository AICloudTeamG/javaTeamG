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

    public AuthService(StaffRepository staffRepository, BCryptPasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ログイン処理を実行します。
     * @param email 入力されたスタッフのメールアドレス
     * @param rawPassword 入力された平文のパスワード
     * @param session HTTPセッション
     * @return ログイン成功したStaffオブジェクト、または認証失敗時にnull
     */
    public Staff login(String email, String rawPassword, HttpSession session) {
        Optional<Staff> staffOptional = staffRepository.findByEmail(email);

        if (staffOptional.isPresent()) {
            Staff staff = staffOptional.get();

            if (!staff.isDeleted() && passwordEncoder.matches(rawPassword, staff.getPassword())) {
                session.setAttribute("loggedInStaff", staff);
                session.setAttribute("loggedInStaffId", staff.getId());
                session.setAttribute("loggedInStaffName", staff.getName());
                session.setAttribute("isAdmin", staff.isAdmin());
                return staff;
            }
        }
        return null;
    }

    /**
     * ログアウト処理を実行し、セッションを無効化します。
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }

    /**
     * 現在のセッションが管理者権限を持っているかを確認します。
     * @param session HTTPセッション
     * @return 管理者であればtrue、そうでなければfalse
     */
    public boolean isAdmin(HttpSession session) {
        Staff loggedInStaff = (Staff) session.getAttribute("loggedInStaff");
        return loggedInStaff != null && loggedInStaff.isAdmin();
    }

    /**
     * 現在ログイン中のスタッフのIDをセッションから取得します。
     * @param session HTTPセッション
     * @return ログイン中のスタッフID、未ログインであればnull
     */
    public Integer getLoggedInStaffId(HttpSession session) {
        Staff loggedInStaff = (Staff) session.getAttribute("loggedInStaff");
        return (loggedInStaff != null) ? loggedInStaff.getId() : null;
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