package com.example.javaTeamG.service;

import com.example.javaTeamG.model.SalesWeather;
import com.example.javaTeamG.model.WeatherCode;
import com.example.javaTeamG.repository.SalesWeatherRepository;
import com.example.javaTeamG.repository.WeatherCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SalesWeatherService {

    private final SalesWeatherRepository salesWeatherRepository;
    private final WeatherCodeRepository weatherCodeRepository;

    public SalesWeatherService(SalesWeatherRepository salesWeatherRepository, WeatherCodeRepository weatherCodeRepository) {
        this.salesWeatherRepository = salesWeatherRepository;
        this.weatherCodeRepository = weatherCodeRepository;
    }

    /**
     * 販売天気情報を保存または更新します。
     * @param salesWeather 保存するSalesWeatherオブジェクト
     * @return 保存されたSalesWeatherオブジェクト
     * @throws IllegalArgumentException 天気コードが見つからない場合
     */
    @Transactional
    public SalesWeather saveSalesWeather(SalesWeather salesWeather) {
        // WeatherCodeが設定されているか、およびそのIDが有効かを確認
        if (salesWeather.getWeatherCode() == null || salesWeather.getWeatherCode().getId() == null) {
            throw new IllegalArgumentException("WeatherCode is required for SalesWeather.");
        }
        
        // 参照されているWeatherCodeが存在するかをDBで確認
        weatherCodeRepository.findById(salesWeather.getWeatherCode().getId())
                .orElseThrow(() -> new IllegalArgumentException("WeatherCode not found with ID: " + salesWeather.getWeatherCode().getId()));

        // 新規作成時はisDeleted=falseを設定。更新時は既存のisDeletedを維持。
        // createdAt/updatedAtは@PrePersist/@PreUpdateで自動設定されるため、ここでは不要。
        if (salesWeather.getId() == null) { 
             salesWeather.setDeleted(false);
        } else { 
            salesWeatherRepository.findById(salesWeather.getId()).ifPresent(existing -> {
                salesWeather.setDeleted(existing.isDeleted());
            });
        }
        
        return salesWeatherRepository.save(salesWeather);
    }

    /**
     * IDに基づいて販売天気情報を検索します。
     * @param id 販売天気ID
     * @return SalesWeatherオブジェクトを含むOptional、または空のOptional
     */
    public Optional<SalesWeather> findSalesWeatherById(Integer id) {
        return salesWeatherRepository.findById(id);
    }

    /**
     * 特定の日付の販売天気情報を検索します。
     * @param date 検索する日付
     * @return SalesWeatherオブジェクトを含むOptional、または空のOptional
     */
    public Optional<SalesWeather> findSalesWeatherByDate(LocalDate date) {
        return salesWeatherRepository.findByDateAndIsDeletedFalse(date);
    }

    /**
     * 全ての販売天気情報を取得します。
     * @return 全販売天気情報のリスト
     */
    public List<SalesWeather> findAllSalesWeather() {
        return salesWeatherRepository.findAll();
    }

    /**
     * 販売天気情報を論理削除します。
     * @param id 削除する販売天気情報のID
     */
    @Transactional
    public void deleteSalesWeather(Integer id) {
        salesWeatherRepository.findById(id)
                .ifPresent(sw -> {
                    sw.setDeleted(true);
                    salesWeatherRepository.save(sw);
                });
    }

    /**
     * デフォルトのWeatherCodeを取得するヘルパーメソッド。
     * 実際のアプリケーションでは、ID=1 をデフォルトとするか、
     * 特定の条件に基づいて取得するか、設定ファイルから読み込むかなどを検討してください。
     * @return デフォルトのWeatherCode
     * @throws IllegalStateException デフォルトのWeatherCodeが見つからない場合
     */
    public WeatherCode getDefaultWeatherCode() {
        // 例: IDが1のWeatherCodeをデフォルトとする
        // 実際のアプリケーションでは、適切なロジックでデフォルトを取得してください
        return weatherCodeRepository.findById(45) // 例としてID=1を使用
            .orElseThrow(() -> new IllegalStateException("Default WeatherCode (ID: 45) not found. Please ensure WeatherCode with ID 1 exists."));
    }
}