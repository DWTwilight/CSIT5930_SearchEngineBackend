package com.hkust.csit5930.searchengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString(callSuper = true)
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class InvertedIndexBase extends EntityBase implements Serializable {
    private String term;

    @Column(name = "documents", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Document> documents;

    // in-memory cache for fast look-up
    @Transient
    private volatile Map<Long, Document> documentMap;

    @NonNull
    public Optional<Document> findDocumentById(@NonNull Long id) {
        if (documentMap == null) {
            documentMap = documents.stream()
                    .collect(Collectors.toUnmodifiableMap(Document::getId, Function.identity()));
        }
        return Optional.ofNullable(documentMap.get(id));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Document implements Serializable {
        private Long id;
        private Long count;
        private List<Long> pos;
    }
}
