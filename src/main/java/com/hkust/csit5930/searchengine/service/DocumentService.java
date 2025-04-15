package com.hkust.csit5930.searchengine.service;

import com.hkust.csit5930.searchengine.entity.DocumentMeta;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

public interface DocumentService {
    long getDocumentCount();

    @NonNull
    Map<Long, DocumentMeta> getMetaByDocumentIds(@NonNull Set<Long> documentId);
}
