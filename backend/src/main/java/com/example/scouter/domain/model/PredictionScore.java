package com.example.scouter.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class PredictionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate targetDate;

    private double predictedScore;

    public PredictionScore(LocalDate targetDate, double predictedScore) {
        this.targetDate = targetDate;
        this.predictedScore = predictedScore;
    }
}