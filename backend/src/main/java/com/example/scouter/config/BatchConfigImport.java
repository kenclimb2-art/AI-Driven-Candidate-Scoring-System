package com.example.scouter.config;

import com.example.scouter.batch.CsvImportTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfigImport {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CsvImportTasklet csvImportTasklet;

    @Bean
    public Step importStep() {
        return new StepBuilder("importCsvStep", jobRepository)
                .tasklet(csvImportTasklet, transactionManager)
                .build();
    }
    @Bean("csvImportJob")
    public Job importUserJob(Step importStep) {
        return new JobBuilder("csvImportJob", jobRepository)
                .start(importStep)
                .build();
    }
}