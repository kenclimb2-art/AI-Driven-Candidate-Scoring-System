package com.example.scouter.domain.model;

import lombok.Data;
import java.time.LocalDate;

/**
 * CSVファイルからデータを読み込むための一時的なDTO
 */
@Data
public class DailyScoreCsv {
    private LocalDate date;
    private int focus;
    private int efficiency;
    private int motivation;
    private int condition;
    private int fatigue;
    private int sleep_quality; // CSVのヘッダ名に合わせる
    private int sexual_desire; // CSVのヘッダ名に合わせる
}