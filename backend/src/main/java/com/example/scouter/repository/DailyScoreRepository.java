package com.example.scouter.repository;

import com.example.scouter.domain.model.DailyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyScoreRepository extends JpaRepository<DailyScore, Long> {
    List<DailyScore> findByTargetDateBetweenOrderByTargetDateAsc(LocalDate startDate, LocalDate endDate);
}