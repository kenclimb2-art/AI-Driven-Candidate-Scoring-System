package com.example.scouter.domain.model;

import java.time.LocalDate;

public class WeeklyPrediction {
    private LocalDate predictDate;
    private Double predictedCondition;

    public WeeklyPrediction(LocalDate predictDate, Double predictedCondition) {
        this.predictDate = predictDate;
        this.predictedCondition = predictedCondition;
    }

    // Getters
    public LocalDate getPredictDate() { return predictDate; }
    public Double getPredictedCondition() { return predictedCondition; }
}