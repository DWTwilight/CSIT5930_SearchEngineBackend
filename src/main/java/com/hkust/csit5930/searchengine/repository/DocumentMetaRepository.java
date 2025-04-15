package com.hkust.csit5930.searchengine.repository;

import com.hkust.csit5930.searchengine.entity.DocumentMeta;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface DocumentMetaRepository {
    @NonNull
    List<DocumentMeta> findAllById(@NonNull Set<Long> ids);

    long count();
}