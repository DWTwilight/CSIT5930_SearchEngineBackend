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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hkust.csit5930.searchengine.constant.CacheConstant.BIGRAM_CACHE;
import static com.hkust.csit5930.searchengine.constant.CacheConstant.INDEX_CACHE_MANAGER;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    private final SearchEngineConfiguration searchEngineConfiguration;
    private final QueryProcessor queryProcessor;
    private final InvertedIndexService invertedIndexService;
    private final DocumentService documentService;
    private final CacheManager cacheManager;

    public SearchServiceImpl(SearchEngineConfiguration searchEngineConfiguration,
                             QueryProcessor queryProcessor,
                             InvertedIndexService invertedIndexService,
                             DocumentService documentService,
                             @Qualifier(INDEX_CACHE_MANAGER) CacheManager cacheManager) {
        this.searchEngineConfiguration = searchEngineConfiguration;
        this.queryProcessor = queryProcessor;
        this.invertedIndexService = invertedIndexService;
        this.documentService = documentService;
        this.cacheManager = cacheManager;
    }

    private static double calculateMagnitude(Map<Long, Double> tfidfVector) {
        return Math.sqrt(tfidfVector.values().stream().mapToDouble(v -> v * v).sum());
    }

    @SuppressWarnings("unchecked")
    private List<Pair<Long, Long>> calculateDocumentBigramMatches(Pair<String, String> bigram, InvertedIndex termAIndex, InvertedIndex termBIndex) {
        var cache = cacheManager.getCache(BIGRAM_CACHE);
        assert cache != null;

        return Optional.ofNullable(cache.get(bigram))
                .map(wrapper -> (List<Pair<Long, Long>>) wrapper.get())
                .orElseGet(() -> {
                    var result = termAIndex.getDocuments().stream()
                            .flatMap(documentTermA ->
                                    Stream.ofNullable(
                                            termBIndex.findDocumentById(documentTermA.id())
                                                    .map(documentTermB -> {
                                                        long count = 0;
                                                        int i = 0, j = 0;
                                                        var posListA = documentTermA.pos();
                                                        var posListB = documentTermB.pos();
                                                        while (i < posListA.size() && j < posListB.size()) {
                                                            var curPosA = posListA.get(i);
                                                            var curPosB = posListB.get(j);
                                                            if (curPosA + 1 == curPosB) {
                                                                count++;
                                                                i++;
                                                                j++;
                                                            } else if (curPosA > curPosB) {
                                                                j++;
                                                            } else {
                                                                i++;
                                                            }
                                                        }
                                                        if (count == 0) {
                                                            return null;
                                                        }
                                                        return Pair.of(documentTermB.id(), count);
                                                    }).orElse(null)))
                            .toList();
                    cache.put(bigram, result);
                    return result;
                });
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
        var queryVector = queryProcessor.vectorize(queryTokens);

        // get inverted indexes
        var bodyIndexMap = invertedIndexService.findBodyIndexByTermIn(queryVector.keySet());
        var titleIndexMap = invertedIndexService.findTitleIndexByTermIn(queryVector.keySet());

        // get candidate document ids
        var titleCandidateDocumentIds = titleIndexMap.values().stream()
                .flatMap(index -> index.getDocuments().stream()
                        .map(InvertedIndex.Document::id))
                .collect(Collectors.toUnmodifiableSet());
        var bodyCandidateDocumentIds = bodyIndexMap.values().stream()
                .flatMap(index -> index.getDocuments().stream()
                        .map(InvertedIndex.Document::id))
                .collect(Collectors.toUnmodifiableSet());
        Set<Long> candidateDocumentIds = Stream.concat(
                        titleCandidateDocumentIds.stream(),
                        bodyCandidateDocumentIds.stream())
                .collect(Collectors.toUnmodifiableSet());
        if (candidateDocumentIds.isEmpty()) {
            return List.of();
        }

        // find query bigrams
        var queryBigrams = queryProcessor.getBigrams(queryTokens);

        // get results
        // 1. calculate relevance score for title and body (tf-idf cosine similarity & 2-gram matching)
        var totalDocumentCount = documentService.getDocumentCount();
        var titleRelevanceScore = calculateRelevanceScore(
                queryVector, queryBigrams, titleCandidateDocumentIds,
                titleIndexMap, totalDocumentCount);
        var bodyRelevanceScore = calculateRelevanceScore(
                queryVector, queryBigrams, bodyCandidateDocumentIds,
                bodyIndexMap, totalDocumentCount);

        // 2. combine title and body scores
        var combinedRelevanceScore = candidateDocumentIds.stream()
                .map(docId -> Pair.of(
                        docId,
                        searchEngineConfiguration.getTitleWeight() * Optional.ofNullable(titleRelevanceScore.get(docId)).orElse(0D)
                                + searchEngineConfiguration.getBodyWeight() * Optional.ofNullable(bodyRelevanceScore.get(docId)).orElse(0D)))
                .filter(docIdScorePair -> docIdScorePair.getSecond() > searchEngineConfiguration.getMinScoreThreshold())
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));

        // 3. get meta and construct results
        var documentMetaMap = documentService.getMetaByDocumentIds(combinedRelevanceScore.keySet());
        return documentMetaMap.values().stream()
                .map(documentMeta -> constructSearchResult(documentMeta, combinedRelevanceScore.get(documentMeta.id())))
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
                                                      Map<Pair<String, String>, Long> queryBigramVector,
                                                      Set<Long> candidateDocumentIds,
                                                      Map<String, InvertedIndex> invertedIndexMap,
                                                      long totalDocumentCount) {
        if (candidateDocumentIds.isEmpty()) {
            return Map.of();
        }
        // 1. calculate tf-idf cosine similarity
        // 1.1 calculate query tf-idf
        long maxTf = Collections.max(queryVector.values());
        var queryTfidfVector = queryVector.entrySet().stream()
                .flatMap(entry ->
                        Stream.ofNullable(invertedIndexMap.get(entry.getKey()))
                                .map(index -> Pair.of(index.getId(), (double) entry.getValue() / maxTf *
                                        Math.log((double) (totalDocumentCount + 1) / (index.getDocuments().size() + 1)))))
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));

        // 1.2 calculate tf-idf cosine similarities
        double queryMagnitude = calculateMagnitude(queryTfidfVector);
        var cosineSimilarityScores = invertedIndexMap.values().stream()
                .flatMap(invertedIndex -> invertedIndex.getDocuments().stream()
                        .map(document -> Pair.of(
                                document.id(),
                                document.tfidfM() * queryTfidfVector.getOrDefault(invertedIndex.getId(), 0D) / queryMagnitude)))
                .collect(Collectors.groupingBy(Pair::getFirst, Collectors.summingDouble(Pair::getSecond)));

        // 2. perform phrase matching(Bigram)
        var bigramMatchScores = queryBigramVector.entrySet().stream()
                .flatMap(bigramFreqPair -> {
                    var bigram = bigramFreqPair.getKey();
                    var termAIndex = invertedIndexMap.get(bigram.getFirst());
                    var termBIndex = invertedIndexMap.get(bigram.getSecond());
                    if (Objects.isNull(termAIndex) || Objects.isNull(termBIndex)) {
                        return Stream.empty();
                    }

                    // calculate bigram tf-idf in all documents
                    // 1. find matches in each doc, list: docId -> normalized tf (tf / max possible count)
                    var documentBigramMatches = calculateDocumentBigramMatches(bigram, termAIndex, termBIndex);
                    if (documentBigramMatches.isEmpty()) {
                        return Stream.empty();
                    }

                    // 2. calculate bigram tf-idf and multiply query bigram freq
                    var documentBigramIdf = Math.log((double) (totalDocumentCount + 1) / (documentBigramMatches.size() + 1));
                    var queryBigramFreq = bigramFreqPair.getValue();
                    return documentBigramMatches.stream()
                            .map(documentBigramMatch ->
                                    Pair.of(documentBigramMatch.getFirst(), documentBigramMatch.getSecond() * documentBigramIdf * queryBigramFreq));
                }).collect(Collectors.groupingBy(Pair::getFirst, Collectors.summingDouble(Pair::getSecond)));

        // 3. normalize and combine the scores
        var maxCosineSimScore = cosineSimilarityScores.isEmpty() ? 1 : Collections.max(cosineSimilarityScores.values());
        var maxBigramScore = bigramMatchScores.isEmpty() ? 1 : Collections.max(bigramMatchScores.values());
        return cosineSimilarityScores.entrySet().stream()
                .map(cosineSim -> Pair.of(cosineSim.getKey(),
                        searchEngineConfiguration.getCosineWeight() * (cosineSim.getValue() / maxCosineSimScore)
                                + Optional.ofNullable(bigramMatchScores.get(cosineSim.getKey()))
                                .map(bigramScore -> searchEngineConfiguration.getBigramWeight() * (bigramScore / maxBigramScore))
                                .orElse(0D)))
                .sorted((a, b) -> b.getSecond().compareTo(a.getSecond()))
                .limit(searchEngineConfiguration.getMaxResultCount())
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));
    }
}
