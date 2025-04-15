package com.hkust.csit5930.searchengine.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkust.csit5930.searchengine.entity.DocumentMeta;
import com.hkust.csit5930.searchengine.repository.DocumentMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DocumentMetaRepositoryImpl implements DocumentMetaRepository {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Long>> FREQ_WORDS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> LINKS_TYPE = new TypeReference<>() {
    };

    private static final RowMapper<DocumentMeta> ROW_MAPPER = (rs, rowNum) -> {
        try {
            return new DocumentMeta(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("url"),
                    rs.getString("last_modified"),
                    rs.getLong("size"),
                    OBJECT_MAPPER.readValue(rs.getString("freq_words"), FREQ_WORDS_TYPE),
                    OBJECT_MAPPER.readValue(rs.getString("parent_links"), LINKS_TYPE),
                    OBJECT_MAPPER.readValue(rs.getString("child_links"), LINKS_TYPE),
                    rs.getLong("max_title_tf"),
                    rs.getLong("max_body_tf"),
                    rs.getDouble("page_rank")
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @NonNull
    @Override
    @Transactional(readOnly = true)
    public List<DocumentMeta> findAllById(@NonNull Set<Long> ids) {
        return jdbcTemplate.query(
                "SELECT * FROM document_meta WHERE id IN (:ids)",
                Map.of("ids", ids),
                ROW_MAPPER
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return Optional.ofNullable(jdbcTemplate.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM document_meta",
                Long.class
        )).orElse(0L);
    }
}
