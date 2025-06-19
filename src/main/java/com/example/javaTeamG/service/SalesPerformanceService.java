package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.model.ProductSalesEntry;
import com.example.javaTeamG.model.SalesPerformance;
import com.example.javaTeamG.model.SalesWeather;
import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.model.WeatherCode; // WeatherCodeをインポート
import com.example.javaTeamG.repository.SalesPerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // BigDecimalをインポート
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SalesPerformanceService {

    private final SalesPerformanceRepository salesPerformanceRepository;
    private final ProductService productService;
    private final AuthService authService;
    private final SalesWeatherService salesWeatherService;

    @Autowired
    public SalesPerformanceService(SalesPerformanceRepository salesPerformanceRepository,
                                   ProductService productService,
                                   AuthService authService,
                                   SalesWeatherService salesWeatherService) {
        this.salesPerformanceRepository = salesPerformanceRepository;
        this.productService = productService;
        this.authService = authService;
        this.salesWeatherService = salesWeatherService;
    }

    // 単一のSalesPerformanceを保存/更新 (既存のCRUD操作用)
    @Transactional
    public SalesPerformance saveSalesPerformance(SalesPerformance salesPerformance) {
        // 更新時、createdAtがnullの場合に既存の値を使うか、明示的にセット
        if (salesPerformance.getId() != null) {
            salesPerformanceRepository.findById(salesPerformance.getId()).ifPresent(existing -> {
                if (salesPerformance.getCreatedAt() == null) {
                    salesPerformance.setCreatedAt(existing.getCreatedAt());
                }
            });
        }
        // 論理削除フラグが明示的にセットされていない場合はfalseにする
        // (新規作成時や、更新時にisDeletedがフォームから送られてこない場合)
        // createdAt/updatedAtは@PrePersist/@PreUpdateで自動設定されるため、ここでは不要。
        if (salesPerformance.getId() == null) { // 新規作成時
            salesPerformance.setDeleted(false);
        } else { // 更新時
             Optional<SalesPerformance> existing = salesPerformanceRepository.findById(salesPerformance.getId());
             existing.ifPresent(sp -> salesPerformance.setDeleted(sp.isDeleted()));
        }

        return salesPerformanceRepository.save(salesPerformance);
    }

    /**
     * 複数の商品販売実績を一度に登録または更新します。
     * 指定された日付と記録者スタッフの既存の販売実績（is_deleted=false）は、
     * まず論理削除（is_deleted=true）し、その後、新しいデータを登録します。
     * これにより、修正（UPDATE）も実質的に対応します。
     *
     * @param date 対象日付
     * @param recorderStaffId 記録者スタッフID
     * @param productSalesEntries 登録/更新する商品販売数リスト
     */
    @Transactional
    public void saveOrUpdateMultipleSalesPerformance(
            LocalDate date, Integer recorderStaffId, List<ProductSalesEntry> productSalesEntries) {

        Staff recordedByStaff = authService.findStaffById(recorderStaffId)
                .orElseThrow(() -> new IllegalArgumentException("記録者スタッフが見つかりません: ID " + recorderStaffId));

        // 当日のSalesWeatherデータを取得または作成
        SalesWeather salesWeather = salesWeatherService.findSalesWeatherByDate(date)
                .orElseGet(() -> {
                    // SalesWeatherが存在しない場合、自動生成する例
                    SalesWeather newWeather = new SalesWeather();
                    newWeather.setDate(date); // dateフィールドを設定

                    // SalesWeatherServiceからデフォルトのWeatherCodeを取得して設定
                    WeatherCode defaultWeatherCode = salesWeatherService.getDefaultWeatherCode();
                    newWeather.setWeatherCode(defaultWeatherCode);

                    // その他の必須フィールドも設定（仮のデフォルト値）
                    // 実際には、天気APIから取得したり、設定で管理したりする
                    newWeather.setTemperatureMax(BigDecimal.valueOf(20.0));
                    newWeather.setTemperatureMin(BigDecimal.valueOf(10.0));
                    newWeather.setTemperatureMean(BigDecimal.valueOf(15.0));
                    newWeather.setHumidityMax(BigDecimal.valueOf(90.0)); 
                    newWeather.setWindspeedMax(BigDecimal.valueOf(5.0));                  
                    newWeather.setDeleted(false); 

                    System.out.println("No SalesWeather found for " + date + ". Creating a dummy one with default WeatherCode ID: " + defaultWeatherCode.getId());
                    // SalesWeatherServiceのsaveSalesWeatherを呼んでDBに保存
                    return salesWeatherService.saveSalesWeather(newWeather);
                });

        // 既存のその日のそのスタッフの「アクティブな」売上実績をすべて論理削除
        List<SalesPerformance> existingActivePerformances = salesPerformanceRepository.findByRecordDateAndRecordedByStaffAndIsDeletedFalse(date, recordedByStaff);
        for (SalesPerformance sp : existingActivePerformances) {
            sp.setDeleted(true); // 論理削除フラグを立てる
            sp.setUpdatedAt(LocalDateTime.now()); // 更新日時を更新
            salesPerformanceRepository.save(sp); // 更新を保存
        }
        
        // 新しい（または更新された）売上実績を登録
        for (ProductSalesEntry entry : productSalesEntries) {
            // 数量が0より大きい場合のみ保存
            if (entry.getQuantity() != null && entry.getQuantity() > 0) {
                Product product = productService.findProductByName(entry.getProductName())
                    .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: " + entry.getProductName()));

                SalesPerformance newPerformance = new SalesPerformance();
                newPerformance.setRecordDate(date);
                newPerformance.setProduct(product);
                newPerformance.setSalesCount(entry.getQuantity());
                newPerformance.setRecordedByStaff(recordedByStaff);
                newPerformance.setSalesWeather(salesWeather); // 取得したSalesWeatherをセット
                newPerformance.setDeleted(false); // 新規作成時は論理削除フラグをfalseに
                
                salesPerformanceRepository.save(newPerformance);
            }
        }
    }

    public Optional<SalesPerformance> findSalesPerformanceById(Integer id) {
        return salesPerformanceRepository.findById(id);
    }

    public List<SalesPerformance> findAllSalesPerformances() {
        return salesPerformanceRepository.findAll();
    }

    public List<SalesPerformance> findAllActiveSalesPerformances() {
        return salesPerformanceRepository.findByIsDeletedFalse();
    }

    public List<SalesPerformance> getSalesPerformanceByDate(LocalDate date) {
        return salesPerformanceRepository.findByRecordDateAndIsDeletedFalse(date);
    }

    @Transactional
    public void deleteSalesPerformance(Integer id) {
        SalesPerformance salesPerformance = salesPerformanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SalesPerformance not found with id: " + id));
        salesPerformance.setDeleted(true);
        salesPerformance.setUpdatedAt(LocalDateTime.now());
        salesPerformanceRepository.save(salesPerformance);
    }
}