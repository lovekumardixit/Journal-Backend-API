package com.love.Backend.controller;

import com.love.Backend.dto.response.SentimentSummaryResponse;
import com.love.Backend.service.BackendEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analytics APIs", description = "APIs for journal mood analytics")
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private BackendEntryService entryService;

    @Operation(summary = "Sentiment summary", description = "Returns sentiment counts for the authenticated user for the last N days")
    @GetMapping("/sentiment-summary")
    public SentimentSummaryResponse getSentimentSummary(@RequestParam(defaultValue = "30") int days) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return entryService.getSentimentSummary(authentication.getName(), days);
    }
}
