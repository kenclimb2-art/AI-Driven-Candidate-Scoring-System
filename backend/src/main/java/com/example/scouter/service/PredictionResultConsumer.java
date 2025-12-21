package com.example.scouter.service;

import com.example.scouter.domain.model.PredictionResponse;
import com.example.scouter.domain.model.PredictionScore;
import com.example.scouter.repository.PredictionScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects; // 追加
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PredictionResultConsumer {

    private final @NonNull PredictionScoreRepository predictionRepository;

    @Transactional
    @KafkaListener(topics = "scouter.prediction.result", groupId = "scouter-java-consumer-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumePredictionResults(@Payload PredictionResponse response) {
        
        if (response == null || response.getPredictions() == null) {
            log.warn(">>>>>> ⚠️ Kafka CONSUMER: 無効なメッセージを受信しました。");
            return;
        }

        // 1. 古い予測データを全削除
        predictionRepository.deleteAllInBatch();

        // 2. 受信したデータをエンティティに変換
        List<PredictionScore> entities = response.getPredictions().stream()
            .map(p -> new PredictionScore(p.getDate(), p.getPredictedScore()))
            .collect(Collectors.toList());

        // 3. DBに保存
        // Objects.requireNonNull を使用して @NonNull Iterable への適合を明示
        if (!entities.isEmpty()) {
            predictionRepository.saveAll(Objects.requireNonNull(entities));
        }

        log.info(">>>>>> ✅ Kafka CONSUMER: 予測データ {} 件をDBに保存しました。 (ID: {})", 
                 entities.size(), response.getMessageId());
    }
}