package com.example.scouter.repository;

import com.example.scouter.domain.model.PredictionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PredictionScoreRepository extends JpaRepository<PredictionScore, Long> {
    List<PredictionScore> findAllByOrderByTargetDateAsc();
}