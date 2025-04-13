package com.hkust.csit5930.searchengine.service;

import com.hkust.csit5930.searchengine.entity.DocumentTfidf;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

public interface DocumentService {
    @NonNull
    Map<Long, DocumentTfidf> getTfidfByDocumentIds(@NonNull Set<Long> documentIds);

    long getDocumentCount();
}
