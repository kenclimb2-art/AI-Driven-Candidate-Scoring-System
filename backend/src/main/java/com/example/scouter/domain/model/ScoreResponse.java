package com.example.scouter.domain.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 画面表示用のデータ転送オブジェクト
 * Record型から通常のClass型に変更し、Thymeleafとの親和性を高める
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreResponse {
    private LocalDate date;
    private int focusScore;
    private int efficiencyScore;
    private int motivationScore;
    private int conditionScore;
    private int disciplineScore;
    private int fatigueScore;
    private int sleepScore;
    private int sexualDesireScore;
    private double avgScore;
    private String overallEvaluation;
}