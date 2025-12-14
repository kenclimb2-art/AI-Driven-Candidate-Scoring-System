package com.example.scouter.domain.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// Pythonの送信データ { "messageId": ..., "predictions": [...] } を受け止めるクラス
@Getter
@Setter
@ToString
public class PredictionResponse {
    private String messageId;
    private List<PredictionData> predictions;
}