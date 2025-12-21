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

    // 個別項目の評価（1-7）
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

    // 総合スコアの評価（0.0-7.0）
    private static final NavigableMap<Double, Supplier<String>> OVERALL_EVALUATION_RULES;
    static {
        TreeMap<Double, Supplier<String>> map = new TreeMap<>();
        map.put(6.0, () -> "S: 覚醒状態");
        map.put(5.0, () -> "A: 高パフォーマンス");
        map.put(4.0, () -> "B: 安定稼働");
        map.put(3.0, () -> "C: 休息推奨");
        map.put(2.0, () -> "D: 要注意・低調"); // 3.0未満
        map.put(0.0, () -> "E: 限界・機能不全"); // 2.0未満
        OVERALL_EVALUATION_RULES = Collections.unmodifiableNavigableMap(map);
    }

    public String getOverallEvaluation(double avgScore) {
        // floorEntryを使って、現在のスコア以下の最大のキー（しきい値）を取得
        Map.Entry<Double, Supplier<String>> entry = OVERALL_EVALUATION_RULES.floorEntry(avgScore);
        return Optional.ofNullable(entry).map(Map.Entry::getValue).map(Supplier::get).orElse("---");
    }
}