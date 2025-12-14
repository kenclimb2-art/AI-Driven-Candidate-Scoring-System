package com.example.scouter.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scouter.domain.logic.ScoreEvaluator;
import com.example.scouter.domain.model.DailyScore;
import com.example.scouter.domain.model.ScoreForm;
import com.example.scouter.repository.DailyScoreRepository; // ★最終修正: 正しいリポジトリパッケージ

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final DailyScoreRepository repository;
    private final ScoreEvaluator scoreEvaluator;

    /**
     * 既存機能: 期間指定でDBからスコアを取得し、評価をつけて返す (照会ボタン押下時)
     * ★NPE回避ロジックを維持。
     */
    public List<Map<String, Object>> getEvaluatedScores(LocalDate startDate, LocalDate endDate) {
        
        List<DailyScore> scores = repository.findAll(); // 全データを取得
        
        // 期間が指定されている場合にのみフィルタリングする (NPE回避)
        if (startDate != null && endDate != null) {
            scores = scores.stream()
                .filter(score -> !score.getTargetDate().isBefore(startDate) && !score.getTargetDate().isAfter(endDate))
                .collect(Collectors.toList());
        }
        // null AND null の場合は、scoresはfindAllの全件リストのまま

        return scores.stream().map(score -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("date", score.getTargetDate());
            map.put("focusScore", score.getFocus());
            map.put("efficiencyScore", score.getEfficiency());
            map.put("motivationScore", score.getMotivation());
            map.put("conditionScore", score.getCondition());
            map.put("fatigueScore", score.getFatigue());
            map.put("sleepScore", score.getSleepQuality());
            map.put("sexualDesireScore", score.getSexualDesire());
            map.put("avgScore", calculateAverage(score));
            map.put("overallEvaluation", scoreEvaluator.getOverallEvaluation(calculateAverage(score)));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 既存機能: 平均点計算
     */
    private double calculateAverage(DailyScore score) {
        return (double) (score.getFocus() + score.getEfficiency() + score.getMotivation() +
                         score.getCondition() + score.getFatigue() + score.getSleepQuality() + 
                         score.getSexualDesire()) / 7.0;
    }

    /**
     * 既存機能: スコア保存
     */
    @Transactional
    public void registerScore(ScoreForm form) {
        DailyScore dailyScore = new DailyScore(); 
        
        dailyScore.setTargetDate(form.getTargetDate()); 
        dailyScore.setFocus(form.getFocus());
        dailyScore.setEfficiency(form.getEfficiency());
        dailyScore.setMotivation(form.getMotivation());
        dailyScore.setCondition(form.getCondition());
        dailyScore.setFatigue(form.getFatigue());
        dailyScore.setSleepQuality(form.getSleepQuality());
        dailyScore.setSexualDesire(form.getSexualDesire());
        
        repository.save(dailyScore);
    }
    
    // ... (その他のメソッドは省略せずにそのまま残してください)
}