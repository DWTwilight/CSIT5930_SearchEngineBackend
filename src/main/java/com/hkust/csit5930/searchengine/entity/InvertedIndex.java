package com.hkust.csit5930.searchengine.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class InvertedIndex {
    private final long id;
    private final String term;
    private final List<Document> documents;

    // in-memory cache for fast look-up
    private volatile Map<Long, Document> documentMap = null;

    @NonNull
    public Optional<Document> findDocumentById(@NonNull Long id) {
        if (documentMap == null) {
            documentMap = documents.stream()
                    .collect(Collectors.toUnmodifiableMap(Document::id, Function.identity()));
        }
        return Optional.ofNullable(documentMap.get(id));
    }

    public record Document(Long id, Long count, List<Long> pos) {
    }
}
