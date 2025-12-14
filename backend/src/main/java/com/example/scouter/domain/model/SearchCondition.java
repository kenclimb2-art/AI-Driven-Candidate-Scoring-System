package com.example.scouter.domain.model;

import java.time.LocalDate;
// Lombokを使用していないことを前提とします

public class SearchCondition {
    private LocalDate startDate;
    private LocalDate endDate;
    
    // 【重要】GetterとSetterを必ず追加してください
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}