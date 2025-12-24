package com.example.scouter.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; 
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;

@Entity
@Data
@NoArgsConstructor
@Slf4j
public class DailyScore {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
    
    private int focus;
    private int efficiency;
    private int motivation;
    private int condition;
    private int fatigue;
    private int sleepQuality;
    private int sexualDesire;
    private int discipline;

    /**
     * ドメインロジック: 総合コンディションスコアの計算 (厳格査定版)
     */
    public double calculateAverage() {
        // 1. 疲労以外の6項目の平均 (1.0 〜 7.0)
        double positiveSum = focus + efficiency + motivation + condition + 
                             discipline + sleepQuality + sexualDesire;
        double baseAvg = positiveSum / 7.0;

        // 2. 疲労ペナルティの基本値 (0.0 〜 1.0)
        double fatiguePenalty = (fatigue - 1) / 6.0;

        // 3. 特殊モード判定
        if (sexualDesire == 7) {
            // 【スーパーリビドーモード：オーバーヒート仕様】
            baseAvg += 0.5;          // 一時的なブースト
            baseAvg -= 1.5;          // 強烈なシステム負荷（固定減点）
            fatiguePenalty *= 3.0;   // 疲労の影響を3倍に増幅（自壊リスク）
            log.error("🚨 [CRITICAL] {} : システムオーバーヒート。即刻休息が必要です。", targetDate);
        } else if (sexualDesire == 1) {
            // 【賢者モード：安定仕様】
            fatiguePenalty *= 0.5;   // 疲労影響を半分に緩和（回復ボーナス）
            log.info("🧘 [{}] 賢者モード検知。安定稼働中。", targetDate);
        }

        // 4. 最終スコア算出
        double finalScore = baseAvg - fatiguePenalty;

        // 1.0 〜 7.0 の範囲に収める
        return Math.min(7.0, Math.max(1.0, finalScore));
    }

    public DailyScore(LocalDate targetDate, int focus, int efficiency,
        int motivation, int condition, int discipline, int fatigue,
        int sleepQuality, int sexualDesire) {
        this.targetDate = targetDate;
        this.focus = focus;
        this.efficiency = efficiency;
        this.motivation = motivation;
        this.condition = condition;
        this.discipline = discipline;
        this.fatigue = fatigue;
        this.sleepQuality = sleepQuality;
        this.sexualDesire = sexualDesire;
    }
}