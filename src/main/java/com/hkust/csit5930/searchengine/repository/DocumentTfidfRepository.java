package com.hkust.csit5930.searchengine.repository;

import com.hkust.csit5930.searchengine.entity.DocumentTfidf;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface DocumentTfidfRepository {
    @NonNull
    List<DocumentTfidf> findAllById(@NonNull Set<Long> ids);
}
