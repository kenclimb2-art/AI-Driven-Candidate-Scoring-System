package com.example.scouter.batch;

import com.example.scouter.domain.model.DailyScore;
import com.example.scouter.repository.DailyScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSVファイルを読み込み、DBに保存するTasklet
 * Taskletは単一の処理（Task）を実行する
 */
@Component
@RequiredArgsConstructor // Lombok: finalフィールドのコンストラクタを自動生成（DI用）
public class CsvImportTasklet implements Tasklet {

    private final DailyScoreRepository repository; // DI: DB操作

    // application.propertiesからファイルパスをDI (SpEL式を使用)
    // CSVファイルをResourceとして取得
    @Value("classpath:scores.csv") 
    private Resource inputResource; 

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution,@NonNull ChunkContext chunkContext) throws Exception {
        
        // データを格納するリスト
        List<DailyScore> scores = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputResource.getInputStream(), StandardCharsets.UTF_8))) {
            
            // ヘッダ行をスキップ
            reader.readLine(); 
            String line;
            
            // Stream APIの学習のため、ここでは伝統的なfor文（whileループ）を使用します。
            // Tasklet内で大規模なStream処理を行うとメモリ負荷が高くなるため、
            // ファイル読み込み処理は古典的に行うのが一般的です。

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 8) continue; // データ不足はスキップ

                // Entityに変換（データの型変換と構築）
                DailyScore score = new DailyScore(
                    LocalDate.parse(fields[0], formatter),
                    Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[2]),
                    Integer.parseInt(fields[3]),
                    Integer.parseInt(fields[4]),
                    Integer.parseInt(fields[5]),
                    Integer.parseInt(fields[6]),
                    Integer.parseInt(fields[7])
                );
                scores.add(score);
            }
        }
        
        // データをDBに一括保存
        repository.saveAll(scores);
        
        // ログ出力
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("importCount", scores.size());
        
        // Taskletの実行が完了したことを示す
        return RepeatStatus.FINISHED;
    }
}