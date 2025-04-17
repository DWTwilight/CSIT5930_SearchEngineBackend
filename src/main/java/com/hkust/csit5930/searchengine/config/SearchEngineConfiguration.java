package com.hkust.csit5930.searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "search-engine")
@Getter
@Setter
public class SearchEngineConfiguration {
    private int nGramCount;

    private double titleWeight;
    private double bodyWeight;

    private double termCosineWeight;
    private double nGramWeight;

    private double relevanceWeight;
    private double pageRankWeight;

    private double minScoreThreshold;

    private long maxResultCount;
}
