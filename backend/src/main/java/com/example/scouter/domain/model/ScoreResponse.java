package com.example.scouter.domain.model;

import java.time.LocalDate;

/**
 * 画面表示用の型安全なデータ転送オブジェクト (Java Record)
 */
public record ScoreResponse(
    LocalDate date,
    int focusScore,
    int efficiencyScore,
    int motivationScore,
    int conditionScore,
    int discipline,
    int fatigueScore,
    int sleepScore,
    int sexualDesireScore,
    double avgScore,
    String overallEvaluation
) {}