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
    private static final NavigableMap<Integer, String> EVALUATION_RULES;
    static {
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(7, "S: 完璧 (MAX)"); 
        map.put(6, "A: 非常に良い"); 
        map.put(5, "B: 良好 (基準達成)");
        map.put(4, "C: 普通 (平均)");
        map.put(3, "D: 普通以下"); 
        map.put(2, "E: 要注意 (低調)");
        map.put(1, "F: 最低 (緊急)"); 
        EVALUATION_RULES = Collections.unmodifiableNavigableMap(map);
    }

    private static final NavigableMap<Double, Supplier<String>> OVERALL_EVALUATION_RULES;
    static {
        TreeMap<Double, Supplier<String>> map = new TreeMap<>();
        map.put(6.0, () -> "S: 覚醒状態");
        map.put(5.0, () -> "A: 高パフォーマンス");
        map.put(4.0, () -> "B: 平均値");
        map.put(3.0, () -> "C: 休息推奨");
        map.put(0.0, () -> "D: データ不足");
        OVERALL_EVALUATION_RULES = Collections.unmodifiableNavigableMap(map);
    }

    public String evaluate(int score) {
        Map.Entry<Integer, String> entry = EVALUATION_RULES.tailMap(score, true).firstEntry();
        return Optional.ofNullable(entry).map(Map.Entry::getValue).orElse("--- (評価不能)");
    }
    
    public String getOverallEvaluation(double avgScore) {
        Map.Entry<Double, Supplier<String>> entry = OVERALL_EVALUATION_RULES.floorEntry(avgScore);
        return Optional.ofNullable(entry).map(Map.Entry::getValue).map(Supplier::get).orElse("---");
    }
}