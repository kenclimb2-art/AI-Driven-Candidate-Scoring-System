package com.example.scouter.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// SseEmitterは削除

import com.example.scouter.domain.model.PredictionData;
import com.example.scouter.domain.model.ScoreForm;
import com.example.scouter.domain.model.SearchForm;
import com.example.scouter.service.BatchRunService;
import com.example.scouter.service.PredictionResultConsumer;
import com.example.scouter.service.ScoreService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@EnableWebMvc
public class WebController {

    private final ScoreService scoreService;
    private final BatchRunService batchRunService;
    private final PredictionResultConsumer predictionResultConsumer;

    public WebController(ScoreService scoreService, BatchRunService batchRunService, PredictionResultConsumer predictionResultConsumer) {
        this.scoreService = scoreService;
        this.batchRunService = batchRunService;
        this.predictionResultConsumer = predictionResultConsumer; 
    }

    // トップページ表示 (GET /)
    @GetMapping("/")
    public String index(Model model) {
        log.info(">>>>>> 📢 GET / リクエスト受信。トップ画面データ取得開始。");
        
        SearchForm searchForm;
        if (!model.containsAttribute("searchForm")) {
            searchForm = new SearchForm(); 
            model.addAttribute("searchForm", searchForm);
        } else {
            searchForm = (SearchForm) model.getAttribute("searchForm");
        }
        
        try {
            List<Map<String, Object>> initialScores = scoreService.getEvaluatedScores(
                searchForm.getStartDate(), 
                searchForm.getEndDate() 
            ); 
            model.addAttribute("scores", initialScores);
            log.info(">>>>>> ✅ GET / リクエスト: 初期データ取得成功。期間({}, {})で{}件。", 
                      searchForm.getStartDate(), searchForm.getEndDate(), initialScores.size());
        } catch (Exception e) {
            log.error(">>>>>> ❌ GET / リクエスト: 初期データ取得エラー: {}", e.getMessage(), e);
            model.addAttribute("scores", Collections.emptyList());
        }

        // Consumerから最新の予測結果を取得し、モデルに追加
        List<PredictionData> predictions = predictionResultConsumer.getLatestPredictions();
        model.addAttribute("predictions", predictions);
        
        return "index";
    }

    // ★削除: SSE接続エンドポイント (/stream/predictions) は削除

    // 入力画面表示 (GET /input)
    @GetMapping("/input")
    public String input(Model model) {
        if (!model.containsAttribute("scoreForm")) {
            model.addAttribute("scoreForm", new ScoreForm());
        }
        return "input";
    }

    // スコア登録処理 (POST /register)
    @PostMapping("/register") 
    public String register(ScoreForm form, RedirectAttributes redirectAttributes) {
        log.info(">>>>>> 📢 POST /register リクエスト受信。登録処理を開始します。"); 
        
        try {
            scoreService.registerScore(form);
            redirectAttributes.addFlashAttribute("message", "スコアを登録しました。");
            log.info(">>>>>> ✅ 登録処理成功。トップへリダイレクトします。");
            return "redirect:/";
        } catch (Exception e) {
            log.error(">>>>>> ❌ 登録処理中に致命的なエラーが発生しました: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "登録中にエラーが発生しました。ログを確認してください。");
            return "redirect:/input"; 
        }
    }

    // 検索処理 (GET /search)
    @GetMapping("/search") 
    public String search(SearchForm form, Model model) {
        log.info(">>>>>> 📢 GET /search リクエスト受信。照会処理を開始します。開始日={}, 終了日={}", 
                  form.getStartDate(), form.getEndDate());

        model.addAttribute("searchForm", form);
        
        try {
            List<Map<String, Object>> evaluatedScores = scoreService.getEvaluatedScores(form.getStartDate(), form.getEndDate());
            model.addAttribute("scores", evaluatedScores); 
            log.info(">>>>>> ✅ 照会処理成功。{}件のデータを取得しました。", evaluatedScores.size());
        } catch (Exception e) {
            log.error(">>>>>> ❌ 照会処理中にエラーが発生しました: {}", e.getMessage(), e);
            model.addAttribute("scores", Collections.emptyList());
        }
        
        List<PredictionData> predictions = predictionResultConsumer.getLatestPredictions();
        model.addAttribute("predictions", predictions);
        
        return "index";
    }
    
    // 予測実行（バッチ）処理
    @PostMapping("/batch/run")
    public String runBatch(RedirectAttributes redirectAttributes) {
        String message = batchRunService.runPredictionEngine();
        redirectAttributes.addFlashAttribute("message", message);
        
        // SSEがなくなったため、リダイレクト後のGET / で最新の予測結果を取得する動作に戻る
        return "redirect:/";
    }
}