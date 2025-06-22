package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.repository.StaffRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffRepository, BCryptPasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 全スタッフ取得（論理削除されていないもののみ）
    public List<Staff> findAllActiveStaffs() {
        return staffRepository.findByIsDeletedFalseOrderByIdAsc();
    }

    // IDでスタッフを取得
    public Optional<Staff> findStaffById(Integer id) {
        return staffRepository.findById(id);
    }

    // メールアドレスでスタッフを取得
    public Optional<Staff> findStaffByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    // スタッフ登録（新規作成）
    @Transactional
    public Staff createStaff(Staff staff) {
        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new IllegalArgumentException("指定されたメールアドレスは既に存在します。");
        }

        staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        staff.setCreatedAt(LocalDateTime.now());
        staff.setUpdatedAt(LocalDateTime.now());
        staff.setDeleted(false); // 論理削除フラグをfalseに設定
        return staffRepository.save(staff);
    }

    // スタッフ情報更新
    @Transactional
    public Staff updateStaff(Integer id, Staff updatedStaff) {
        return staffRepository.findById(id).map(staff -> {
            if (!staff.getEmail().equals(updatedStaff.getEmail())
                    && staffRepository.existsByEmail(updatedStaff.getEmail())) {
                throw new IllegalArgumentException("指定されたメールアドレスは既に他のアカウントで使用されています。");
            }
            staff.setName(updatedStaff.getName());
            staff.setEmail(updatedStaff.getEmail());
            staff.setAdmin(updatedStaff.isAdmin());
            staff.setDeleted(updatedStaff.isDeleted());
            staff.setUpdatedAt(LocalDateTime.now());
            return staffRepository.save(staff);
        }).orElseThrow(() -> new IllegalArgumentException("スタッフが見つかりません。ID: " + id));
    }


    // パスワードリセット
    @Transactional
    public Staff resetPassword(Integer id, String newPassword) {
        return staffRepository.findById(id).map(staff -> {
            
            staff.setPassword(passwordEncoder.encode(newPassword)); 
            staff.setUpdatedAt(LocalDateTime.now());
            return staffRepository.save(staff);
        }).orElseThrow(() -> new IllegalArgumentException("スタッフが見つかりません。ID: " + id));
    }

    // スタッフ削除（論理削除）
    @Transactional
    public void deleteStaff(Integer id) {
        staffRepository.findById(id).ifPresent(staff -> {
            staff.setDeleted(true);
            staff.setUpdatedAt(LocalDateTime.now());
            staffRepository.save(staff);
        });
    }

    // スタッフが存在するかどうか
    public boolean staffExists(Integer id) {
        return staffRepository.existsById(id);
    }
}