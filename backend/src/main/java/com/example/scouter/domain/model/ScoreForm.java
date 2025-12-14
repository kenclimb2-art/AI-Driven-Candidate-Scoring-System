package com.example.scouter.domain.model;
// または com.example.scouter.domain.model;

import java.time.LocalDate;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat; // ★追加

@Data
public class ScoreForm {

    @NotNull(message = "日付は必須です")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // ★修正: 日付フォーマットを明示的に指定
    private LocalDate targetDate = LocalDate.now(); // ★修正: 本日日付をデフォルトに

    @Min(value = 1, message = "スコアは1以上です")
    @Max(value = 7, message = "スコアは7以下です")
    private Integer focus = 3; // ★修正: デフォルト値を 3 に
    
    @Min(value = 1, message = "スコアは1以上です")
    @Max(value = 7, message = "スコアは7以下です")
    private Integer efficiency = 3; // ★修正: デフォルト値を 3 に
    
    @Min(value = 1, message = "スコアは1以上です")
    @Max(value = 7, message = "スコアは7以下です")
    private Integer motivation = 3; // ★修正: デフォルト値を 3 に

    @Min(value = 1, message = "スコアは1以上です")
    @Max(value = 7, message = "スコアは7以下です")
    private Integer condition = 3; // ★修正: デフォルト値を 3 に
    
    @Min(value = 1, message = "スコアは1以上です")
    @Max(value = 7, message = "スコアは7以下です")
    private Integer fatigue = 3; // ★修正: デフォルト値を 3 に

    @Min(value = 1, message = "スコアは1以上です")
    @Max(value = 7, message = "スコアは7以下です")
    private Integer sleepQuality = 3; // ★修正: デフォルト値を 3 に

    @Min(value = 1, message = "スコアは1以上です")
    @Max(value = 7, message = "スコアは7以下です")
    private Integer sexualDesire = 3; // ★修正: デフォルト値を 3 に
}