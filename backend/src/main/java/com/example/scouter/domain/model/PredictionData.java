package com.example.scouter.domain.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty; // ★追加：Jacksonアノテーション

// PythonからKafkaで送られてくる予測結果の単一データを格納するクラス
public class PredictionData {
    // Python側が "date" キーで送ってくる (YYYY-MM-DD形式)
    private LocalDate date; 

    // ★修正箇所: Pythonが "predicted_score" で送るため、マッピングを強制する
    @JsonProperty("predicted_score")
    private Double predictedScore;

    // GetterとSetter (Springのデシリアライズに必要)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getPredictedScore() {
        return predictedScore;
    }

    public void setPredictedScore(Double predictedScore) {
        this.predictedScore = predictedScore;
    }

    @Override
    public String toString() {
        return "{" + date + ", score=" + predictedScore + "}";
    }
}