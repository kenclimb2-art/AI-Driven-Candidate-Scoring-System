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
 */
@Component
@RequiredArgsConstructor
public class CsvImportTasklet implements Tasklet {

    private final DailyScoreRepository repository;

    @Value("classpath:scores.csv")
    private Resource inputResource;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {

        List<DailyScore> scores = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputResource.getInputStream(), StandardCharsets.UTF_8))) {

            // ヘッダ行をスキップ
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                // 最小限必要なフィールド（日付〜性欲スコアまで）があるかチェック
                if (fields.length < 8) continue;

                // 1. まず空っぽの箱（インスタンス）を作る
                // ※Entity側に @NoArgsConstructor が必要です
                DailyScore score = new DailyScore();

                // 2. CSVにあるデータだけをセットする
                // ※Entity側に @Setter が必要です
                score.setTargetDate(LocalDate.parse(fields[0], formatter));
                score.setFocus(Integer.parseInt(fields[1]));
                score.setEfficiency(Integer.parseInt(fields[2]));
                score.setMotivation(Integer.parseInt(fields[3]));
                score.setCondition(Integer.parseInt(fields[4]));
                score.setFatigue(Integer.parseInt(fields[5]));
                score.setSleepQuality(Integer.parseInt(fields[6]));
                score.setSexualDesire(Integer.parseInt(fields[7]));

                // ★「規律(discipline)」はセットしていないので、
                // Entity側で定義したデフォルト値（private int discipline = 3;）が適用されます。

                scores.add(score);
            }
        }

        // データをDBに一括保存
        if (!scores.isEmpty()) {
            repository.saveAll(scores);
        }

        // ログ用に件数を保存
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("importCount", scores.size());

        return RepeatStatus.FINISHED;
    }
}