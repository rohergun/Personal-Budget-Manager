package io.github.rohergun.budgetmanager.summary;

import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.summary.dto.MonthlySummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month) {

        YearMonth resolvedMonth = month != null ? month : YearMonth.now();

        return ResponseEntity.ok(
                summaryService.getMonthlyTransactionsSummary(principal.getId(), resolvedMonth)
        );
    }
}
