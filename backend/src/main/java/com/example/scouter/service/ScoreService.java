package com.example.scouter.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scouter.domain.logic.ScoreEvaluator;
import com.example.scouter.domain.model.DailyScore;
import com.example.scouter.domain.model.ScoreForm;
import com.example.scouter.domain.model.ScoreResponse; // 追加
import com.example.scouter.repository.DailyScoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final @NonNull DailyScoreRepository repository;
    private final @NonNull ScoreEvaluator scoreEvaluator;

    public List<ScoreResponse> getEvaluatedScores(LocalDate startDate, LocalDate endDate) {
        List<DailyScore> scores = (startDate != null && endDate != null)
                ? repository.findByTargetDateBetweenOrderByTargetDateAsc(startDate, endDate)
                : repository.findAll();

        return scores.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * EntityからDTOへの変換ロジック
     */
    private ScoreResponse convertToResponse(DailyScore score) {
        double avg = score.calculateAverage();
        return new ScoreResponse(
            score.getTargetDate(),
            score.getFocus(),
            score.getEfficiency(),
            score.getMotivation(),
            score.getCondition(),
            score.getDiscipline(),
            score.getFatigue(),
            score.getSleepQuality(),
            score.getSexualDesire(),
            avg,
            scoreEvaluator.getOverallEvaluation(avg)
        );
    }

    @Transactional
    public void registerScore(ScoreForm form) {
        DailyScore dailyScore = repository.findByTargetDate(form.getTargetDate())
                .orElseGet(DailyScore::new);
        
        dailyScore.setTargetDate(form.getTargetDate()); 
        dailyScore.setFocus(form.getFocus());
        dailyScore.setEfficiency(form.getEfficiency());
        dailyScore.setMotivation(form.getMotivation());
        dailyScore.setCondition(form.getCondition());
        dailyScore.setDiscipline(form.getDiscipline());
        dailyScore.setFatigue(form.getFatigue());
        dailyScore.setSleepQuality(form.getSleepQuality());
        dailyScore.setSexualDesire(form.getSexualDesire());
        
        repository.save(dailyScore);
    }
}