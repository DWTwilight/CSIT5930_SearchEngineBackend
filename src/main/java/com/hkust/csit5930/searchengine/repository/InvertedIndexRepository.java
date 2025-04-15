package com.hkust.csit5930.searchengine.repository;

import com.hkust.csit5930.searchengine.entity.InvertedIndex;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface InvertedIndexRepository {
    @NonNull
    List<InvertedIndex> findTitleIndexByTermIn(@NonNull Set<String> terms);

    @NonNull
    List<InvertedIndex> findBodyIndexByTermIn(@NonNull Set<String> terms);
}
