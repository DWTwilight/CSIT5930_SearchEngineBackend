package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.config.SearchEngineConfiguration;
import com.hkust.csit5930.searchengine.entity.DocumentMeta;
import com.hkust.csit5930.searchengine.entity.InvertedIndex;
import com.hkust.csit5930.searchengine.response.data.SearchResult;
import com.hkust.csit5930.searchengine.service.DocumentService;
import com.hkust.csit5930.searchengine.service.InvertedIndexService;
import com.hkust.csit5930.searchengine.service.SearchService;
import com.hkust.csit5930.searchengine.util.QueryProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    private final SearchEngineConfiguration searchEngineConfiguration;
    private final QueryProcessor queryProcessor;
    private final InvertedIndexService invertedIndexService;
    private final DocumentService documentService;

    public SearchServiceImpl(SearchEngineConfiguration searchEngineConfiguration,
                             QueryProcessor queryProcessor,
                             InvertedIndexService invertedIndexService,
                             DocumentService documentService) {
        this.searchEngineConfiguration = searchEngineConfiguration;
        this.queryProcessor = queryProcessor;
        this.invertedIndexService = invertedIndexService;
        this.documentService = documentService;
    }

    @Override
    @NonNull
    public List<SearchResult> search(@NonNull String query) {
        if (query.isBlank()) {
            return List.of();
        }

        // tokenize with stop word removal and stemming
        var queryTokens = queryProcessor.tokenize(query);
        log.debug("tokenized query: {}", queryTokens);
        if (queryTokens.isEmpty()) {
            return List.of();
        }
        // get query vector
        var queryVector = queryProcessor.vectorizeWithNGram(queryTokens, searchEngineConfiguration.getNGramCount());
        log.debug("query vector: {}", queryVector);

        // get inverted indexes
        var bodyIndexMap = invertedIndexService.findBodyIndexByTermIn(queryVector.keySet());
        var titleIndexMap = invertedIndexService.findTitleIndexByTermIn(queryVector.keySet());
        if (titleIndexMap.isEmpty() && bodyIndexMap.isEmpty()) {
            return List.of();
        }

        // get results
        // 1. calculate relevance score for title and body (tf-idf cosine similarity & n-gram matching)
        var totalDocumentCount = documentService.getDocumentCount();
        var titleRelevanceScore = calculateRelevanceScore(queryVector, titleIndexMap, totalDocumentCount);
        var bodyRelevanceScore = calculateRelevanceScore(queryVector, bodyIndexMap, totalDocumentCount);

        // 2. combine title and body scores
        var combinedRelevanceScores = Stream.concat(
                        titleRelevanceScore.entrySet().stream()
                                .map(scorePair -> Pair.of(scorePair.getKey(), scorePair.getValue() * searchEngineConfiguration.getTitleWeight())),
                        bodyRelevanceScore.entrySet().stream()
                                .map(scorePair -> Pair.of(scorePair.getKey(), scorePair.getValue() * searchEngineConfiguration.getBodyWeight())))
                .collect(Collectors.groupingBy(Pair::getFirst, Collectors.summingDouble(Pair::getSecond)))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(searchEngineConfiguration.getMaxResultCount())
                .toList();

        // 3. get meta and construct results
        var documentMetaMap = documentService.getMetaByDocumentIds(combinedRelevanceScores.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet()));
        return combinedRelevanceScores.stream()
                .flatMap(entry -> Stream.ofNullable(documentMetaMap.get(entry.getKey()))
                        .map(documentMeta -> constructSearchResult(documentMeta, entry.getValue())))
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .toList();
    }

    private SearchResult constructSearchResult(DocumentMeta documentMeta, double relevanceScore) {
        return new SearchResult(
                documentMeta.id(),
                searchEngineConfiguration.getRelevanceWeight() * relevanceScore
                        + searchEngineConfiguration.getPageRankWeight() * documentMeta.pageRank(),
                documentMeta.title(), documentMeta.url(), documentMeta.lastModified(), documentMeta.size(),
                documentMeta.frequentWords(), documentMeta.parentLinks(), documentMeta.childLinks());
    }

    private Map<Long, Double> calculateRelevanceScore(Map<String, Long> queryVector,
                                                      Map<String, InvertedIndex> invertedIndexMap,
                                                      long totalDocumentCount) {
        if (invertedIndexMap.isEmpty()) {
            return Map.of();
        }

        var similarityScores = queryVector.entrySet().stream()
                .flatMap(entry ->
                        Stream.ofNullable(invertedIndexMap.get(entry.getKey()))
                                .flatMap(index -> {
                                    var weight = index.ngram() > 1 ? searchEngineConfiguration.getNGramWeight() : searchEngineConfiguration.getTermCosineWeight();
                                    var queryTermTfidf = (double) entry.getValue() * weight *
                                            Math.log((double) (totalDocumentCount + 1) / (index.documents().size() + 1));
                                    return index.documents().entrySet().stream()
                                            .map(documentTfidfEntry -> Pair.of(
                                                    documentTfidfEntry.getKey(),
                                                    documentTfidfEntry.getValue() * queryTermTfidf));
                                }))
                .collect(Collectors.groupingBy(Pair::getFirst, Collectors.summingDouble(Pair::getSecond)));

        var maxSimilarityScore = Collections.max(similarityScores.values());
        return similarityScores.entrySet().stream()
                .filter(scorePair -> scorePair.getValue() > searchEngineConfiguration.getMinScoreThreshold())
                .map(scorePair -> Pair.of(scorePair.getKey(), scorePair.getValue() / maxSimilarityScore))
                .sorted((a, b) -> b.getSecond().compareTo(a.getSecond()))
                .limit(searchEngineConfiguration.getMaxResultCount())
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));
    }
}
