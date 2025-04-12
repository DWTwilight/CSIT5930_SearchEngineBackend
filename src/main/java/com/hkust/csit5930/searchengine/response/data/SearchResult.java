package com.hkust.csit5930.searchengine.response.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record SearchResult(Long id, Double score, String title, String url, String lastModified, Long size,
                           @JsonProperty("freqWords") Map<String, Long> frequentWords, List<String> parentLinks,
                           List<String> childLinks) {
}
