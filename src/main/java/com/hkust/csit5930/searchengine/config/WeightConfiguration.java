package com.hkust.csit5930.searchengine.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "search-engine:weight")
@Getter
public class WeightConfiguration {
    private double titleWeight;
    private double bodyWeight;
    private double biGramBoost;
    private double pageRankWeight;
}
