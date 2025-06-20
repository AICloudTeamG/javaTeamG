package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.SalesWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional; 
import java.util.List;

@Repository
public interface SalesWeatherRepository extends JpaRepository<SalesWeather, Integer> {
    Optional<SalesWeather> findByDateAndIsDeletedFalse(LocalDate date);

        // 指定された期間内の天気情報を取得（WeatherCodeをEAGERフェッチ）
    @Query("SELECT sw FROM SalesWeather sw JOIN FETCH sw.weatherCode WHERE sw.date BETWEEN :startDate AND :endDate AND sw.isDeleted = false ORDER BY sw.date ASC")
    List<SalesWeather> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}