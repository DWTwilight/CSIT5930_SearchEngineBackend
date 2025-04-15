package com.hkust.csit5930.searchengine.service;

import com.hkust.csit5930.searchengine.entity.InvertedIndex;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

public interface InvertedIndexService {
    @NonNull
    Map<String, InvertedIndex> findBodyIndexByTermIn(@NonNull Set<String> terms);

    @NonNull
    Map<String, InvertedIndex> findTitleIndexByTermIn(@NonNull Set<String> terms);
}
