package com.example.scouter.config;

import com.example.scouter.batch.DailyScoreCsvExportTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfigExport {

    // フィールドを削除し、各Beanメソッドの引数でインジェクションする形式に変更
    // これにより、SpringがBean生成時に「絶対に非Nullであること」を保証するため、警告が消えます

    @Bean
    public Step exportStep(
            @NonNull JobRepository jobRepository, 
            @NonNull PlatformTransactionManager transactionManager,
            @NonNull DailyScoreCsvExportTasklet exportTasklet) {
        
        return new StepBuilder("exportDailyScoreStep", jobRepository)
                .tasklet(exportTasklet, transactionManager)
                .build();
    }

    @Bean("dailyScoreExportJob")
    public Job dailyScoreExportJob(
            @NonNull JobRepository jobRepository, 
            @NonNull Step exportStep) {
        
        return new JobBuilder("dailyScoreExportJob", jobRepository)
                .start(exportStep)
                .build();
    }
}