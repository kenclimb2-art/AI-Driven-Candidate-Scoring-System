package com.example.scouter.config;

import com.example.scouter.batch.CsvImportTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // 追加
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
// @RequiredArgsConstructor は削除します
public class BatchConfigImport {

    @Bean
    public Step importStep(
            @NonNull JobRepository jobRepository, 
            @NonNull PlatformTransactionManager transactionManager, 
            @NonNull CsvImportTasklet csvImportTasklet) {
        
        return new StepBuilder("importCsvStep", jobRepository)
                .tasklet(csvImportTasklet, transactionManager)
                .build();
    }

    @Bean("csvImportJob")
    public Job importUserJob(
            @NonNull JobRepository jobRepository, 
            @NonNull Step importStep) {
        
        return new JobBuilder("csvImportJob", jobRepository)
                .start(importStep)
                .build();
    }
}