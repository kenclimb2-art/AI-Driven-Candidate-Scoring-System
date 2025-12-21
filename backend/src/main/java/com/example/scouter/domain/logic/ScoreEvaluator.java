package com.example.scouter.domain.logic;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

@Component
public class ScoreEvaluator {

    // 警告が出ていたフィールド（個別スコア用）
    private static final NavigableMap<Integer, String> EVALUATION_RULES;
    static {
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(7, "S: 完璧"); 
        map.put(6, "A: 良好"); 
        map.put(5, "B: 基準");
        map.put(4, "C: 普通");
        map.put(3, "D: 低調"); 
        map.put(2, "E: 警告");
        map.put(1, "F: 限界"); 
        EVALUATION_RULES = Collections.unmodifiableNavigableMap(map);
    }

    // 総合スコア用
    private static final NavigableMap<Double, Supplier<String>> OVERALL_EVALUATION_RULES;
    static {
        TreeMap<Double, Supplier<String>> map = new TreeMap<>();
        map.put(6.0, () -> "S: 覚醒状態");
        map.put(5.0, () -> "A: 高パフォーマンス");
        map.put(4.0, () -> "B: 安定稼働");
        map.put(3.0, () -> "C: 休息推奨");
        map.put(2.0, () -> "D: 要注意・低調");
        map.put(0.0, () -> "E: 限界・機能不全");
        OVERALL_EVALUATION_RULES = Collections.unmodifiableNavigableMap(map);
    }

    /**
     * ★追加: 個別スコア（1-7）を評価するメソッド
     * これを追加することで EVALUATION_RULES が使用され、警告が消えます。
     */
    public String evaluate(int score) {
        // floorEntry: 指定されたキー以下の最大のキーに対応するエントリを返す
        Map.Entry<Integer, String> entry = EVALUATION_RULES.floorEntry(score);
        return Optional.ofNullable(entry).map(Map.Entry::getValue).orElse("---");
    }
    
    /**
     * 総合平均スコアを評価するメソッド
     */
    public String getOverallEvaluation(double avgScore) {
        Map.Entry<Double, Supplier<String>> entry = OVERALL_EVALUATION_RULES.floorEntry(avgScore);
        return Optional.ofNullable(entry).map(Map.Entry::getValue).map(Supplier::get).orElse("---");
    }
}