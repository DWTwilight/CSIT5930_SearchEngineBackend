package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.response.data.SearchResult;
import com.hkust.csit5930.searchengine.service.InvertedIndexService;
import com.hkust.csit5930.searchengine.service.SearchService;
import com.hkust.csit5930.searchengine.util.QueryProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final QueryProcessor queryProcessor;
    private final InvertedIndexService invertedIndexService;

    @Override
    @NonNull
    public List<SearchResult> search(@NonNull String query) {
        if (query.isBlank()) {
            return List.of();
        }

        // tokenize with stop word removal and stemming
        var tokens = queryProcessor.tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }

        // get query vector
        var queryVector = queryProcessor.vectorize(tokens);

        // get inverted indexes
        var bodyIndex = invertedIndexService.findBodyIndexByTermIn(queryVector.keySet());
        var titleIndex = invertedIndexService.findTitleIndexByTermIn(queryVector.keySet());

        return List.of();
    }
}
