package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.SalesPerformance;
import com.example.javaTeamG.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesPerformanceRepository extends JpaRepository<SalesPerformance, Integer> {
    // 指定された日付の、論理削除されていない販売実績を取得
    List<SalesPerformance> findByRecordDateAndIsDeletedFalse(LocalDate recordDate);

    // 指定された日付と記録者スタッフの、論理削除されていない販売実績を取得
    List<SalesPerformance> findByRecordDateAndRecordedByStaffAndIsDeletedFalse(LocalDate recordDate, Staff recordedByStaff);

    // 全ての論理削除されていない販売実績を取得
    List<SalesPerformance> findByIsDeletedFalse();
}