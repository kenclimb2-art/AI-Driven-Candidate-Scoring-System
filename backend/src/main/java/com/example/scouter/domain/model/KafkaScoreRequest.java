package com.example.scouter.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaScoreRequest {
    // リクエストのID。返信時にどのリクエストの結果か紐づけるのに使う
    private String messageId; 
    
    // 予測の対象日
    private String targetDate; 
    
    // Pythonが学習に使う履歴データ
    private List<DailyScore> history;

    public KafkaScoreRequest(List<DailyScore> history) {
        this.messageId = UUID.randomUUID().toString();
        this.history = history;
        // ターゲット日はPythonで計算するので、ここでは空欄でもOK
        this.targetDate = null; 
    }
}