package com.example.javaTeamG.service;

import com.example.javaTeamG.model.OrderPredictionData; 
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class OrderPredictionService { 

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OrderPredictionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // LocalDate を処理できるように登録
    }

    // 外部APIから予測データを取得するメソッド (WebClientの代わりにダミーAPIを呼び出す例)
    public List<OrderPredictionData> fetchPredictionDataFromExternalApi(String apiUrl) throws IOException {
        try {
            String jsonResponse = webClient.get()
                    .uri(apiUrl) // コントローラーのダミーデータエンドポイントを呼び出す
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 同期的にレスポンスを待つ (本番では非同期処理を検討)

            return parsePredictionData(jsonResponse);
        } catch (Exception e) {
            // エラーログ出力
            System.err.println("Error fetching prediction data from external API: " + e.getMessage());
            // 例外を再度スローするか、空リストを返すか、ビジネスロジックによる
            throw new IOException("Failed to fetch prediction data from external API", e);
        }
    }

    // JSON文字列をPredictionDataオブジェクトのリストにパースするメソッド
    public List<OrderPredictionData> parsePredictionData(String jsonData) throws IOException {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonData, new TypeReference<List<OrderPredictionData>>() {});
        } catch (IOException e) {
            System.err.println("Error parsing JSON data: " + e.getMessage());
            throw e;
        }
    }
}