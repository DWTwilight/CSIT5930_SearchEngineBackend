package com.hkust.csit5930.searchengine.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkust.csit5930.searchengine.entity.DocumentTfidf;
import com.hkust.csit5930.searchengine.repository.DocumentTfidfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DocumentTfidfRepositoryImpl implements DocumentTfidfRepository {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<Long, Double>> TFIDF_VECTOR_TYPE = new TypeReference<>() {
    };
    private static final RowMapper<DocumentTfidf> ROW_MAPPER = (rs, rowNum) -> {
        try {
            return new DocumentTfidf(
                    rs.getLong("id"),
                    OBJECT_MAPPER.readValue(rs.getString("title_tfidf_vec"), TFIDF_VECTOR_TYPE),
                    rs.getDouble("title_mag"),
                    OBJECT_MAPPER.readValue(rs.getString("body_tfidf_vec"), TFIDF_VECTOR_TYPE),
                    rs.getDouble("body_mag")
            );
        } catch (JsonProcessingException e) {
            throw new DataRetrievalFailureException("JSONB deserialization failed", e);
        }
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @NonNull
    @Override
    @Transactional(readOnly = true)
    public List<DocumentTfidf> findAllById(@NonNull Set<Long> ids) {
        return jdbcTemplate.query(
                "SELECT id, title_tfidf_vec, title_mag, body_tfidf_vec, body_mag " +
                        "FROM document_tfidf WHERE id IN (:ids)",
                Map.of("ids", ids),
                ROW_MAPPER
        );
    }
}
