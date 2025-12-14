package com.example.scouter.config;

import com.example.scouter.batch.DailyScoreCsvExportTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfigExport {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DailyScoreCsvExportTasklet exportTasklet;

    public BatchConfigExport(JobRepository jobRepository, 
                             PlatformTransactionManager transactionManager,
                             DailyScoreCsvExportTasklet exportTasklet) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.exportTasklet = exportTasklet;
    }

    @Bean
    public Step exportStep() {
        return new StepBuilder("exportDailyScoreStep", jobRepository)
                .tasklet(exportTasklet, transactionManager)
                .build();
    }

    @Bean("dailyScoreExportJob")
    public Job dailyScoreExportJob(Step exportStep) {
        return new JobBuilder("dailyScoreExportJob", jobRepository)
                .start(exportStep)
                .build();
    }
}