package com.hkust.csit5930.searchengine.service;

import com.hkust.csit5930.searchengine.entity.InvertedIndexBase;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface InvertedIndexService {
    @NonNull
    List<InvertedIndexBase> findBodyIndexByTermIn(@NonNull Set<String> terms);

    @NonNull
    List<InvertedIndexBase> findTitleIndexByTermIn(@NonNull Set<String> terms);
}
