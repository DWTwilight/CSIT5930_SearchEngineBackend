package com.hkust.csit5930.searchengine.entity;

import java.util.Map;

public record InvertedIndex(long id, String term, int ngram, Map<Long, Double> documents) {
}
