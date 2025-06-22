package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByEmail(String email); // ログインにメールアドレスを使用する前提
    boolean existsByEmail(String email); // メールアドレスが既に存在するか確認
    List<Staff> findByIsDeletedFalse(); // 削除されていないスタッフのみ取得
        
    List<Staff> findByIsDeletedFalseOrderByIdAsc();// 論理削除されていないスタッフをIDの昇順で取得するメソッド
}