package com.example.javaTeamG.service;

import com.example.javaTeamG.model.Product;
import com.example.javaTeamG.model.ProductSalesEntry;
import com.example.javaTeamG.model.SalesPerformance;
import com.example.javaTeamG.model.SalesWeather;
import com.example.javaTeamG.model.Staff;
import com.example.javaTeamG.model.WeatherCode;
import com.example.javaTeamG.repository.SalesPerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * 指定された日付と記録者スタッフの既存の販売実績（is_deleted=false）に対して、
     * 入力された商品販売数に基づいて更新（または新規登録）を行います。
     * 数量が0の場合でもレコードを保存し、論理削除は行いません。
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
                    newWeather.setDate(date);

                    // SalesWeatherServiceからデフォルトのWeatherCodeを取得して設定
                    WeatherCode defaultWeatherCode = salesWeatherService.getDefaultWeatherCode();
                    newWeather.setWeatherCode(defaultWeatherCode);

                    // その他の必須フィールドも設定（仮のデフォルト値）
                    newWeather.setTemperatureMax(BigDecimal.valueOf(0.0));
                    newWeather.setTemperatureMin(BigDecimal.valueOf(0.0));
                    newWeather.setTemperatureMean(BigDecimal.valueOf(0.0));
                    newWeather.setHumidityMax(BigDecimal.valueOf(0.0)); 
                    newWeather.setHumidityMin(BigDecimal.valueOf(0.0)); 
                    newWeather.setWindspeedMax(BigDecimal.valueOf(0.0)); 
                    newWeather.setDeleted(false); 

                    System.out.println("No SalesWeather found for " + date + ". Creating a dummy one with default WeatherCode ID: " + defaultWeatherCode.getId());
                    return salesWeatherService.saveSalesWeather(newWeather);
                });

        // 既存のその日の「有効な（isDeleted=false）」売上実績を取得
        // 製品名でマップ化しておくと、後続の処理で効率的に検索できる
        Map<String, SalesPerformance> existingActivePerformancesMap = salesPerformanceRepository.findByRecordDateAndIsDeletedFalse(date)
            .stream()
            .collect(Collectors.toMap(sp -> sp.getProduct().getName(), sp -> sp));

        // 入力された販売実績を処理
        for (ProductSalesEntry entry : productSalesEntries) {
            String productName = entry.getProductName();
            // 数量がnullの場合は0として扱う
            Integer newQuantity = entry.getQuantity() != null ? entry.getQuantity() : 0; 

            // 商品エンティティを取得
            Product product = productService.findProductByName(productName)
                    .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: " + productName));

            // 既存のパフォーマンスを探す
            SalesPerformance existingPerformance = existingActivePerformancesMap.get(productName);

            if (existingPerformance != null) {
                // 既存のレコードがある場合、数量を更新
                existingPerformance.setSalesCount(newQuantity);
                existingPerformance.setRecordedByStaff(recordedByStaff); // 記録者を更新
                existingPerformance.setSalesWeather(salesWeather); // 天気情報を更新
                // 0件保存の場合、deletedフラグは常にfalseを維持 (または変更しない)
                existingPerformance.setDeleted(false); 
                salesPerformanceRepository.save(existingPerformance);
            } else {
                // 既存のレコードがない場合、新規登録
                // 数量が0の場合でも新規レコードを作成
                SalesPerformance newPerformance = new SalesPerformance();
                newPerformance.setRecordDate(date);
                newPerformance.setProduct(product);
                newPerformance.setSalesCount(newQuantity);
                newPerformance.setRecordedByStaff(recordedByStaff);
                newPerformance.setSalesWeather(salesWeather); 
                newPerformance.setDeleted(false); // 新規作成時は常にfalse
                salesPerformanceRepository.save(newPerformance);
            }
            // 処理したものはマップから削除
            existingActivePerformancesMap.remove(productName); 
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

    /**
     * 指定された日付の有効な（論理削除されていない）販売実績を取得します。
     * @param date 検索対象日付
     * @return その日付の販売実績のリスト
     */
    public List<SalesPerformance> getSalesPerformanceByDate(LocalDate date) {
        // 0件のデータも取得対象となるため、isDeletedFalseのままでOK
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