package com.example.javaTeamG.service;

import com.example.javaTeamG.model.SalesPerformance;
import com.example.javaTeamG.repository.SalesPerformanceRepository;
import com.example.javaTeamG.repository.ProductRepository;
import com.example.javaTeamG.repository.StaffRepository;
import com.example.javaTeamG.repository.SalesWeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SalesPerformanceService {

    private final SalesPerformanceRepository salesPerformanceRepository;
    private final ProductRepository productRepository;
    private final StaffRepository staffRepository;
    private final SalesWeatherRepository salesWeatherRepository;

    public SalesPerformanceService(SalesPerformanceRepository salesPerformanceRepository,
                                   ProductRepository productRepository,
                                   StaffRepository staffRepository,
                                   SalesWeatherRepository salesWeatherRepository) {
        this.salesPerformanceRepository = salesPerformanceRepository;
        this.productRepository = productRepository;
        this.staffRepository = staffRepository;
        this.salesWeatherRepository = salesWeatherRepository;
    }

    /**
     * 販売実績を新規登録または更新します。
     * @param salesPerformance 登録または更新する販売実績オブジェクト
     * @return 登録または更新された販売実績オブジェクト
     * @throws IllegalArgumentException 関連エンティティ（商品、スタッフ、天気）が見つからない場合やバリデーションエラー
     */
    @Transactional
    public SalesPerformance saveSalesPerformance(SalesPerformance salesPerformance) {
        // 関連エンティティの存在チェック
        productRepository.findById(salesPerformance.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        staffRepository.findById(salesPerformance.getRecordedByStaff().getId())
                .orElseThrow(() -> new IllegalArgumentException("Staff not found."));
        salesWeatherRepository.findById(salesPerformance.getSalesWeather().getId())
                .orElseThrow(() -> new IllegalArgumentException("SalesWeather not found."));

        // sales_count のバリデーション (上限1000)
        if (salesPerformance.getSalesCount() <= 0 || salesPerformance.getSalesCount() > 1000) {
            throw new IllegalArgumentException("Sales count must be between 1 and 1000.");
        }

        salesPerformance.setDeleted(false);
        return salesPerformanceRepository.save(salesPerformance);
    }

    /**
     * IDに基づいて販売実績を検索します。
     * @param id 販売実績ID
     * @return 販売実績オブジェクトを含むOptional、または空のOptional
     */
    public Optional<SalesPerformance> findSalesPerformanceById(Integer id) {
        return salesPerformanceRepository.findById(id);
    }

    /**
     * 全ての販売実績（論理削除されたものを含む可能性あり）を取得します。
     * @return 全販売実績のリスト
     */
    public List<SalesPerformance> findAllSalesPerformances() {
        return salesPerformanceRepository.findAll();
    }

    /**
     * 特定の日付の販売実績を検索します。
     * @param date 検索する日付
     * @return その日の販売実績のリスト
     */
    public List<SalesPerformance> findSalesPerformanceByDate(LocalDate date) {
        return salesPerformanceRepository.findByRecordDateAndIsDeletedFalse(date);
    }

    /**
     * 販売実績を論理削除します。
     * @param id 削除する販売実績のID
     */
    @Transactional
    public void deleteSalesPerformance(Integer id) {
        salesPerformanceRepository.findById(id)
                .ifPresent(sp -> {
                    sp.setDeleted(true);
                    salesPerformanceRepository.save(sp);
                });
    }
}
