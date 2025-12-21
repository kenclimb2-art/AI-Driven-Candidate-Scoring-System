package com.example.scouter.domain.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class ScoreForm {
    @NotNull(message = "日付は必須です")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate = LocalDate.now(); // デフォルト：本日日付

    @Min(1) @Max(7)
    private int focus = 3; // デフォルト：3

    @Min(1) @Max(7)
    private int efficiency = 3; // デフォルト：3

    @Min(1) @Max(7)
    private int motivation = 3; // デフォルト：3

    @Min(1) @Max(7)
    private int condition = 3; // デフォルト：3

    @Min(1) @Max(7)
    private int fatigue = 3; // デフォルト：3

    @Min(1) @Max(7)
    private int sleepQuality = 3; // デフォルト：3

    @Min(1) @Max(7)
    private int sexualDesire = 5; // デフォルト：5（性欲だけ5）
}