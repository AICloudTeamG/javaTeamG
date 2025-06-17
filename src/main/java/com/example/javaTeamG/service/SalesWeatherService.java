package com.example.javaTeamG.service;

import com.example.javaTeamG.model.SalesWeather;
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
        weatherCodeRepository.findById(salesWeather.getWeatherCode().getId())
                .orElseThrow(() -> new IllegalArgumentException("WeatherCode not found."));

        salesWeather.setDeleted(false);
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
     * @return SalesWeatherオブジェクト、または見つからない場合はnull
     */
    public SalesWeather findSalesWeatherByDate(LocalDate date) {
        return salesWeatherRepository.findByDateAndIsDeletedFalse(date);
    }

    /**
     * 全ての販売天気情報を取得します。
     * @return 全販売天気情報のリスト
     */
    public List<SalesWeather> findAllSalesWeather() {
        return salesWeatherRepository.findAll(); // 論理削除されたデータを除く場合はカスタムクエリが必要
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
}