package com.example.scouter.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor; 
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat; // ★追加：Jacksonアノテーション

@Entity
@Data
@NoArgsConstructor
public class DailyScore {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ★修正箇所: LocalDateをJSONに出力する際、"YYYY-MM-DD"形式の文字列に変換するよう強制
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
    
    private int focus;
    private int efficiency;
    private int motivation;
    private int condition;
    private int fatigue;
    private int sleepQuality;
    private int sexualDesire;

    public DailyScore(LocalDate targetDate, int focus, int efficiency, int motivation, int condition, int fatigue, int sleepQuality, int sexualDesire) {
        this.targetDate = targetDate;
        this.focus = focus;
        this.efficiency = efficiency;
        this.motivation = motivation;
        this.condition = condition;
        this.fatigue = fatigue;
        this.sleepQuality = sleepQuality;
        this.sexualDesire = sexualDesire;
    }
}