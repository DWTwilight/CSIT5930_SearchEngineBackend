package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.config.WeightConfiguration;
import com.hkust.csit5930.searchengine.entity.DocumentTfidf;
import com.hkust.csit5930.searchengine.entity.InvertedIndexBase;
import com.hkust.csit5930.searchengine.response.data.SearchResult;
import com.hkust.csit5930.searchengine.service.DocumentService;
import com.hkust.csit5930.searchengine.service.InvertedIndexService;
import com.hkust.csit5930.searchengine.service.SearchService;
import com.hkust.csit5930.searchengine.util.QueryProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final QueryProcessor queryProcessor;
    private final InvertedIndexService invertedIndexService;
    private final DocumentService documentService;
    private final WeightConfiguration weightConfiguration;

    @Override
    @NonNull
    public List<SearchResult> search(@NonNull String query) {
        if (query.isBlank()) {
            return List.of();
        }

        // tokenize with stop word removal and stemming
        var queryTokens = queryProcessor.tokenize(query);
        if (queryTokens.isEmpty()) {
            return List.of();
        }
        // get query vector
        var queryVector = queryProcessor.vectorize(queryTokens);

        // get inverted indexes
        var bodyIndexMap = invertedIndexService.findBodyIndexByTermIn(queryVector.keySet());
        var titleIndexMap = invertedIndexService.findTitleIndexByTermIn(queryVector.keySet());

        // get candidate document ids
        var titleCandidateDocumentIds = bodyIndexMap.values().stream()
                .flatMap(index -> index.getDocuments().stream()
                        .map(InvertedIndexBase.Document::getId)).distinct()
                .collect(Collectors.toUnmodifiableSet());
        var bodyCandidateDocumentIds = titleIndexMap.values().stream()
                .flatMap(index -> index.getDocuments().stream()
                        .map(InvertedIndexBase.Document::getId)).distinct()
                .collect(Collectors.toUnmodifiableSet());
        Set<Long> candidateDocumentIds = Stream.concat(
                        titleCandidateDocumentIds.stream(),
                        bodyCandidateDocumentIds.stream())
                .collect(Collectors.toUnmodifiableSet());
        if (candidateDocumentIds.isEmpty()) {
            return List.of();
        }

        // get pre-computed document tf-idf vectors
        var documentTfidfMap = documentService.getTfidfByDocumentIds(candidateDocumentIds);

        // calculate relevance score (tf-idf cosine similarity & 2-gram matching)
        // title


        return List.of();
    }

    private Map<Long, Double> calculateRelevanceScore(List<QueryProcessor.Token> queryTokens,
                                                      Map<String, Long> queryVector,
                                                      Set<Long> candidateDocumentIds,
                                                      Map<String, InvertedIndexBase> invertedIndexMap,
                                                      Map<Long, DocumentTfidf> documentTfidfMap,
                                                      long documentCount) {
        // 1. calculate tf-idf cosine similarity
        // 1.1 calculate query tf-idf
        long maxTf = Collections.max(queryVector.values());
        var queryTfidfVector = queryVector.entrySet().stream()
                .flatMap(entry ->
                        Stream.ofNullable(invertedIndexMap.get(entry.getKey()))
                                .map(index -> Pair.of(index.getId(), (double) entry.getValue() / maxTf *
                                        Math.log((double) (documentCount + 1) / ((long) index.getDocuments().size() + 1)))))
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));

        // 1.2 calculate tf-idf cosine similarities
        var cosineSimilarityScores = candidateDocumentIds.stream()
                .flatMap(docId -> Stream.ofNullable(documentTfidfMap.get(docId)))
                .map(documentTfidf -> Pair.of(documentTfidf.getId(), calculateCosineSimilarity(queryTfidfVector, documentTfidf.getTitleTfidfVector())))
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));

        // 2. perform phrase matching

        // 3. combine the scores and return
        return Map.of();
    }

    private double calculateCosineSimilarity(Map<Long, Double> queryTfidfVector, Map<Long, Double> docTfidfVec) {
        var dotProduct = queryTfidfVector.entrySet().stream()
                .flatMapToDouble(queryEntry -> Stream.ofNullable(docTfidfVec.get(queryEntry.getKey()))
                        .mapToDouble(docScore -> queryEntry.getValue() * docScore))
                .sum();
        return dotProduct / (calculateMagnitude(queryTfidfVector) * calculateMagnitude(docTfidfVec));
    }

    private double calculateMagnitude(Map<Long, Double> tfidfVector) {
        return Math.sqrt(tfidfVector.values().stream().mapToDouble(v -> v * v).sum());
    }
}
