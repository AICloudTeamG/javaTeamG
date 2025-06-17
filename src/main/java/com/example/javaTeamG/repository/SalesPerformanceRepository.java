package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.SalesPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesPerformanceRepository extends JpaRepository<SalesPerformance, Integer> {
    // 例: 特定日付の削除されていない販売実績を取得
    List<SalesPerformance> findByRecordDateAndIsDeletedFalse(LocalDate recordDate);
    // 必要に応じてカスタムクエリを追加
}