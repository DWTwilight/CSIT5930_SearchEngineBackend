package com.hkust.csit5930.searchengine.service;

import com.hkust.csit5930.searchengine.entity.InvertedIndexBase;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

public interface InvertedIndexService {
    @NonNull
    Map<String, InvertedIndexBase> findBodyIndexByTermIn(@NonNull Set<String> terms);

    @NonNull
    Map<String, InvertedIndexBase> findTitleIndexByTermIn(@NonNull Set<String> terms);
}
