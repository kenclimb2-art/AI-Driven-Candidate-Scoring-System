package com.example.scouter.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class SearchForm {
    @NotNull(message = "開始日は必須です")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate = LocalDate.now().withDayOfMonth(1);

    @NotNull(message = "終了日は必須です")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate = LocalDate.now();
}