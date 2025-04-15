package com.hkust.csit5930.searchengine.entity;

import java.util.Map;

public record DocumentTfidf(long id,
                            Map<Long, Double> titleTfidfVector, double titleMagnitude,
                            Map<Long, Double> bodyTfidfVector, double bodyMagnitude) implements Identifiable {
}
