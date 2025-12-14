package com.example.scouter.service;

import com.example.scouter.domain.model.PredictionData;
import com.example.scouter.domain.model.PredictionResponse; 
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
// SSE関連のimportは削除 (SseEmitter, IOException, CopyOnWriteArrayList)

import java.util.Collections;
import java.util.List;
// CopyOnWriteArrayList は削除

@Service
public class PredictionResultConsumer {

    // 予測結果を一時的にメモリに保持するための変数
    private volatile List<PredictionData> latestPredictions = Collections.emptyList();

    // SSE接続リスト (sseEmitters) は削除
    
    // Pythonが結果を返すトピック名
    private static final String TOPIC_OUTPUT = "scouter.prediction.result";

    /**
     * Kafkaから予測結果メッセージを受信するメソッド。
     */
    @KafkaListener(topics = TOPIC_OUTPUT, groupId = "scouter-java-consumer-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumePredictionResults(PredictionResponse response) {
        
        // リストは response オブジェクトから取得する
        List<PredictionData> predictions = response != null ? response.getPredictions() : Collections.emptyList();
        
        if (!predictions.isEmpty()) {
            this.latestPredictions = predictions;
            System.out.println(">>>>>> ✅ Kafka CONSUMER: 予測結果メッセージを受信しました。データ数: " + predictions.size());
            System.out.println(">>>>>> 🕒 受信データ (ID: " + response.getMessageId() + "): " + predictions.toString());
            // SSE通知ロジック (notifyClients) は削除
        } else {
            System.out.println(">>>>>> ⚠️ Kafka CONSUMER: 空の予測結果を受信しました。");
        }
    }

    // addEmitter() メソッドは削除

    // notifyClients() メソッドは削除
    
    /**
     * Webコントローラやサービスから、最新の予測結果を取得するためのゲッター。
     */
    public List<PredictionData> getLatestPredictions() {
        return latestPredictions;
    }
    
    /**
     * 予測結果をクリアするメソッド（オプション）。
     */
    public void clearPredictions() {
        this.latestPredictions = Collections.emptyList();
    }
}