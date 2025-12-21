package com.example.scouter.service;

import java.util.List;
import java.util.Objects;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scouter.domain.model.DailyScore;
import com.example.scouter.domain.model.KafkaScoreRequest;
import com.example.scouter.repository.DailyScoreRepository;
import com.example.scouter.repository.PredictionScoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchRunService {

    // 全てのフィールドに @NonNull を付与し、Lombokのコンストラクタ注入を確実にします
    private final @NonNull DailyScoreRepository dailyScoreRepository;
    private final @NonNull KafkaTemplate<String, KafkaScoreRequest> kafkaTemplate;
    private final @NonNull PredictionScoreRepository predictionScoreRepository;

    /**
     * Python AI Engineにデータを送り、予測を依頼する (Producer)
     * 削除処理を含むため、readOnly = true は外します。
     */
    @Transactional
    public String runPredictionEngine() {
        log.info("--- 🚀 Kafka連携: 予測エンジン起動処理開始 ---");

        // 1. 新しい予測を開始するので、古い予測をクリアする（ポーリング検知用）
        predictionScoreRepository.deleteAllInBatch();

        // 既存ロジック維持: 直前のDB書き込みのコミット完了を待つためのスリープ
        try {
            Thread.sleep(100); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. DBから全履歴データを取得
        List<DailyScore> historyData = dailyScoreRepository.findAll();
        
        if (historyData.isEmpty()) {
            log.warn("履歴データがありません。予測をスキップします。");
            return "履歴データがありません。";
        }
        
        // 3. Kafkaリクエスト用のDTOを作成
        KafkaScoreRequest request = new KafkaScoreRequest(historyData);

        // 4. Kafka Topicにメッセージを送信 (Produce)
        final String topic = "scouter.score.input";
        
        // Objects.requireNonNull を使用して、引数が @NonNull String であることを保証
        kafkaTemplate.send(
            Objects.requireNonNull(topic), 
            Objects.requireNonNull(request.getMessageId()), 
            request
        ); 
        
        log.info("✅ Kafka Topic '{}' に予測依頼メッセージ (ID: {}) を送信しました。", topic, request.getMessageId());
        
        return "予測依頼をAIエンジンに送信しました。結果は非同期で反映されます。";
    }
}