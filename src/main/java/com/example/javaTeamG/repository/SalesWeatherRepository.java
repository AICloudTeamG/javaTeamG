package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.SalesWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface SalesWeatherRepository extends JpaRepository<SalesWeather, Integer> {
    SalesWeather findByDateAndIsDeletedFalse(LocalDate date);
}