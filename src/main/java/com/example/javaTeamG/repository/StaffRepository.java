package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Staff findByEmail(String email); // メールアドレスでスタッフを検索
    boolean existsByEmail(String email); // メールアドレスの存在チェック
}