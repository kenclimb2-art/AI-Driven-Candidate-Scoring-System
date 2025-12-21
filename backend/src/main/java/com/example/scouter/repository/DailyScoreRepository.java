package com.example.scouter.repository;

import com.example.scouter.domain.model.DailyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // 追加

@Repository
public interface DailyScoreRepository extends JpaRepository<DailyScore, Long> {
    // 期間検索（既存）
    List<DailyScore> findByTargetDateBetweenOrderByTargetDateAsc(LocalDate startDate, LocalDate endDate);
    
    // 特定の日付で検索（上書き判定用に追加）
    Optional<DailyScore> findByTargetDate(LocalDate targetDate);
}