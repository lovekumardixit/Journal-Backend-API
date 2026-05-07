package com.love.Backend.dto.response;

import com.love.Backend.enums.Sentiment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class SentimentSummaryResponse {

    private long totalEntries; 

    private Map<Sentiment, Long> countBySentiment; 
}
