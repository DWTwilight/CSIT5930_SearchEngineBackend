package com.hkust.csit5930.searchengine.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkust.csit5930.searchengine.entity.InvertedIndex;
import com.hkust.csit5930.searchengine.repository.InvertedIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RegisterReflectionForBinding(InvertedIndex.Document.class)
@Repository
@RequiredArgsConstructor
public class InvertedIndexRepositoryImpl implements InvertedIndexRepository {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<InvertedIndex.Document>> DOCUMENT_LIST_TYPE_REFERENCE = new TypeReference<>() {
    };
    private static final RowMapper<InvertedIndex> ROW_MAPPER = (rs, rowNum) -> {
        try {
            return new InvertedIndex(
                    rs.getLong("id"),
                    rs.getString("term"),
                    OBJECT_MAPPER.readValue(rs.getString("documents"), DOCUMENT_LIST_TYPE_REFERENCE));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public List<InvertedIndex> findTitleIndexByTermIn(@NonNull Set<String> terms) {
        return jdbcTemplate.query(
                "SELECT id, term, documents FROM title_inverted_index WHERE term IN (:terms)",
                Map.of("terms", terms),
                ROW_MAPPER);
    }

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public List<InvertedIndex> findBodyIndexByTermIn(@NonNull Set<String> terms) {
        return jdbcTemplate.query(
                "SELECT id,term,documents FROM body_inverted_index WHERE term IN (:terms)",
                Map.of("terms", terms),
                ROW_MAPPER);
    }
}
