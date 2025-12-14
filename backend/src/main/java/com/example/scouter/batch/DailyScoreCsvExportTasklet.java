package com.example.scouter.batch;

import com.example.scouter.domain.model.DailyScore;
import com.example.scouter.repository.DailyScoreRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DailyScoreCsvExportTasklet implements Tasklet {

    private final DailyScoreRepository repository;

    // ★修正ポイント: 
    // "data/..." と書くことで、実行時のカレントディレクトリ(scouterフォルダ)の直下に
    // dataフォルダを探しに行きます。resources配下にはなりません。
    private static final String CSV_FILE_PATH = "data/input_daily_scores.csv";

    public DailyScoreCsvExportTasklet(DailyScoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<DailyScore> scores = repository.findAll(); 
        
        // 1. ファイルオブジェクトを作成（相対パス）
        File file = new File(CSV_FILE_PATH);
        
        // 2. 親ディレクトリ（dataフォルダ）の確認と作成
        File parentDir = file.getParentFile();

        // フォルダがない場合は自動作成する
        if (parentDir != null && !parentDir.exists()) {
            System.out.println("出力用フォルダを作成します: " + parentDir.getAbsolutePath());
            parentDir.mkdirs();
        }

        // 3. 書き込み実行
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // ヘッダー
            pw.println("target_date,focus,efficiency,motivation,condition,fatigue,sleep_quality,sexual_desire");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (DailyScore score : scores) {
                String line = String.format("%s,%d,%d,%d,%d,%d,%d,%d",
                    score.getTargetDate().format(formatter),
                    score.getFocus(), score.getEfficiency(), score.getMotivation(),
                    score.getCondition(), score.getFatigue(), score.getSleepQuality(), score.getSexualDesire()
                );
                pw.println(line);
            }
        }
        
        // ログ出力（絶対パスを表示して、どこに保存されたか確認しやすくする）
        System.out.println("✅ CSV Exported Path: " + file.getAbsolutePath());
        
        return RepeatStatus.FINISHED;
    }
}