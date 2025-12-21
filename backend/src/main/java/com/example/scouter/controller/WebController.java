package com.example.scouter.controller;

import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.scouter.domain.model.ScoreForm;
import com.example.scouter.domain.model.SearchForm;
import com.example.scouter.domain.model.ScoreResponse;
import com.example.scouter.domain.model.PredictionScore; // 追加
import com.example.scouter.repository.PredictionScoreRepository; // 追加
import com.example.scouter.service.BatchRunService;
import com.example.scouter.service.ScoreService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class WebController {

    private final ScoreService scoreService;
    private final BatchRunService batchRunService;
    private final PredictionScoreRepository predictionScoreRepository; // 修正

    public WebController(
            @NonNull ScoreService scoreService, 
            @NonNull BatchRunService batchRunService, 
            @NonNull PredictionScoreRepository predictionScoreRepository) { // 修正
        this.scoreService = scoreService;
        this.batchRunService = batchRunService;
        this.predictionScoreRepository = predictionScoreRepository; 
    }

    @ModelAttribute("searchForm")
    public SearchForm setUpSearchForm() {
        return new SearchForm();
    }

    @GetMapping("/")
    public String index(Model model, @ModelAttribute("searchForm") SearchForm searchForm) {
        log.info(">>>>>> 📢 GET / リクエスト受信。");
        populateModelWithScores(model, searchForm);
        return "index";
    }

    @GetMapping("/input")
    public String input(Model model) {
        if (!model.containsAttribute("scoreForm")) {
            model.addAttribute("scoreForm", new ScoreForm());
        }
        return "input";
    }

    @PostMapping("/register") 
    public String register(@Valid @ModelAttribute("scoreForm") ScoreForm form, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "input";
        }
        try {
            scoreService.registerScore(form);
            redirectAttributes.addFlashAttribute("message", "スコアを登録・更新しました。");
            return "redirect:/";
        } catch (Exception e) {
            log.error(">>>>>> ❌ 登録エラー: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "登録中にエラーが発生しました。");
            return "redirect:/input"; 
        }
    }

    @GetMapping("/search") 
    public String search(@ModelAttribute("searchForm") SearchForm form, Model model) {
        populateModelWithScores(model, form);
        return "index";
    }
    
    @PostMapping("/batch/run")
    public String runBatch(RedirectAttributes redirectAttributes) {
        String message = batchRunService.runPredictionEngine();
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/";
    }

    // WebController.java に追加
    @GetMapping("/api/predictions")
    @ResponseBody // JSONとして返す
    public List<PredictionScore> getPredictionsApi() {
        return predictionScoreRepository.findAllByOrderByTargetDateAsc();
    }

    private void populateModelWithScores(Model model, SearchForm searchForm) {
        try {
            List<ScoreResponse> scores = scoreService.getEvaluatedScores(
                searchForm.getStartDate(), 
                searchForm.getEndDate() 
            ); 
            model.addAttribute("scores", scores);

            // ★修正: DBから最新の予測データを取得
            List<PredictionScore> predictions = predictionScoreRepository.findAllByOrderByTargetDateAsc();
            model.addAttribute("predictions", predictions);

        } catch (Exception e) {
            log.error(">>>>>> ❌ データ取得エラー: {}", e.getMessage());
            model.addAttribute("scores", Collections.emptyList());
        }
    }
}