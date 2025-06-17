package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.repository.StaffRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 新しいスタッフアカウントを登録します。パスワードはハッシュ化されます。
     * @param staff 登録するスタッフ情報（平文のパスワードを含む）
     * @return 登録されたスタッフオブジェクト
     * @throws IllegalArgumentException メールアドレスが既に存在する場合
     */
    @Transactional
    public Staff createStaff(Staff staff) {
        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }
        staff.setPassword(passwordEncoder.encode(staff.getPassword())); // パスワードをハッシュ化
        staff.setDeleted(false); // 新規作成時は未削除
        return staffRepository.save(staff);
    }

    /**
     * IDに基づいてスタッフを検索します。
     * @param id スタッフID
     * @return スタッフオブジェクトを含むOptional、または空のOptional
     */
    public Optional<Staff> findStaffById(Integer id) {
        return staffRepository.findById(id);
    }

    /**
     * 全てのスタッフ（論理削除されたスタッフを含む可能性あり）を取得します。
     * 論理削除されたスタッフを除外する場合は、StaffRepositoryにカスタムメソッドを追加し、それを利用してください。
     * @return 全スタッフのリスト
     */
    public List<Staff> findAllStaffs() {
        return staffRepository.findAll();
    }

    /**
     * 既存のスタッフ情報を更新します。パスワードは変更しません。
     * @param id 更新するスタッフのID
     * @param updatedStaff 更新情報を含むスタッフオブジェクト
     * @return 更新されたスタッフオブジェクト
     * @throws RuntimeException 指定されたIDのスタッフが見つからない場合
     */
    @Transactional
    public Staff updateStaff(Integer id, Staff updatedStaff) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setName(updatedStaff.getName());
                    staff.setEmail(updatedStaff.getEmail()); // メールアドレスの重複チェックは別途必要になる場合あり
                    staff.setAdmin(updatedStaff.isAdmin());
                    // パスワードは別途 changePassword メソッドを使用
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * スタッフアカウントを論理削除します。
     * @param id 削除するスタッフのID
     */
    @Transactional
    public void deleteStaff(Integer id) {
        staffRepository.findById(id)
                .ifPresent(staff -> {
                    staff.setDeleted(true); // is_deleted フラグをtrueに設定
                    staffRepository.save(staff);
                });
    }

    /**
     * スタッフのパスワードを変更します。
     * @param staffId パスワードを変更するスタッフのID
     * @param newPassword 新しい平文のパスワード
     * @return 更新されたスタッフオブジェクト
     * @throws RuntimeException 指定されたIDのスタッフが見つからない場合
     */
    @Transactional
    public Staff changePassword(Integer staffId, String newPassword) {
        return staffRepository.findById(staffId)
                .map(staff -> {
                    staff.setPassword(passwordEncoder.encode(newPassword));
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));
    }
}