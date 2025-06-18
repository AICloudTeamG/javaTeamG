// package com.example.javaTeamG.service;

// import com.example.javaTeamG.model.OrderPrediction; // OrderPredictionモデルはまだ定義していませんが、後で追加します
// import com.example.javaTeamG.repository.ProductRepository;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate; // Pythonサービスとの連携に利用

// import java.time.LocalDate;
// import java.util.List;

// @Service
// public class OrderPredictionService {

//     private final ProductRepository productRepository;
//     private final RestTemplate restTemplate; // Pythonサービスとの通信用

//     // Python予測モデルのAPIエンドポイント (例)
//     private static final String PYTHON_PREDICTION_API_URL = "http://localhost:5000/predict";

//     public OrderPredictionService(ProductRepository productRepository) {
//         this.productRepository = productRepository;
//         this.restTemplate = new RestTemplate(); // RestTemplateを初期化
//     }

//     /**
//      * 指定された週のビール発注予測を取得します。
//      * 実際には、販売実績や天気情報などをPython予測モデルに渡し、結果を受け取ります。
//      * @param weekStartDate 週の開始日（例: 月曜日）
//      * @return 予測結果のリスト（仮のデータ構造）
//      */
//     public List<OrderPrediction> getWeeklyOrderPrediction(LocalDate weekStartDate) {
//         // ここにPython予測モデルとの連携ロジックを実装します。
//         // 例: RestTemplateを使ってPythonサービスにHTTPリクエストを送信
//         // Map<String, Object> requestData = new HashMap<>();
//         // requestData.put("start_date", weekStartDate.toString());
//         // requestData.put("sales_data", salesService.getSalesDataForPrediction(weekStartDate)); // 仮のメソッド
//         // requestData.put("weather_data", salesWeatherService.getWeatherDataForPrediction(weekStartDate)); // 仮のメソッド

//         // OrderPrediction[] predictions = restTemplate.postForObject(PYTHON_PREDICTION_API_URL, requestData, OrderPrediction[].class);
//         // return Arrays.asList(predictions);

//         // 今はダミーデータを返します。
//         System.out.println("Calling Python model for prediction for week starting: " + weekStartDate);
//         return List.of(
//             new OrderPrediction(1, weekStartDate, productRepository.findById(1).orElse(null), 100),
//             new OrderPrediction(2, weekStartDate, productRepository.findById(2).orElse(null), 150)
//         );
//     }
// }