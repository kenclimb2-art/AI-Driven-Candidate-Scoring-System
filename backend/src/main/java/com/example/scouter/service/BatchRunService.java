package com.example.scouter.service;

import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scouter.domain.model.DailyScore;
import com.example.scouter.domain.model.KafkaScoreRequest;
import com.example.scouter.repository.DailyScoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchRunService {

    private final DailyScoreRepository dailyScoreRepository;
    private final KafkaTemplate<String, KafkaScoreRequest> kafkaTemplate; // ★NEW: KafkaTemplateを注入

    // Python AI Engineにデータを送り、予測を依頼する (Producer)
    @Transactional(readOnly = true)
    public String runPredictionEngine() {
        log.info("--- 🚀 Kafka連携: 予測エンジン起動処理開始 ---");

        // ★修正箇所: -------------------------------------------------------------
        // 直前のDB書き込み処理（データ登録）のコミット完了を待つため、短時間スリープ
        try {
            // 100ミリ秒待機。これにより、ほとんどの環境で最新データが読み取れるようになる
            Thread.sleep(100); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // ----------------------------------------------------------------------

        // 1. DBから全履歴データを取得
        // Kafkaに送るデータは、AIエンジンが処理しやすいように全履歴とする
        List<DailyScore> historyData = dailyScoreRepository.findAll();
        
        if (historyData.isEmpty()) {
            log.warn("履歴データがありません。予測をスキップします。");
            return "履歴データがありません。";
        }
        
        // 2. Kafkaリクエスト用のDTOを作成
        // このDTOがJSON形式でKafkaに送られます
        KafkaScoreRequest request = new KafkaScoreRequest(historyData);

        // 3. Kafka Topicにメッセージを送信 (Produce)
        final String topic = "scouter.score.input";
        
        // keyとしてmessageIdを使うと、同じIDのメッセージが同じパーティションに送られやすくなる
        kafkaTemplate.send(topic, request.getMessageId(), request); 
        
        log.info("✅ Kafka Topic '{}' に予測依頼メッセージ (ID: {}) を送信しました。", topic, request.getMessageId());
        
        // 4. ファイル連携と違い、ここではPythonの完了を待たない（非同期）
        return "予測依頼をAIエンジンに送信しました。結果は非同期で反映されます。";
    }

    /* * NOTE: 以前のファイル出力やProcessBuilder関連のメソッドは全て削除されました。
     */
}