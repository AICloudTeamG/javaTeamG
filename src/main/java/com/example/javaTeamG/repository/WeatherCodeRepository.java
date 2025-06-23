package com.example.javaTeamG.repository;

import com.example.javaTeamG.model.WeatherCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WeatherCodeRepository extends JpaRepository<WeatherCode, Integer> {
    // WeatherCodeはIDが主キーなので、特別な検索メソッドは不要な場合が多い
    Optional<WeatherCode> findById(Integer id);
}