package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.entity.DocumentTfidf;
import com.hkust.csit5930.searchengine.repository.DocumentMetaRepository;
import com.hkust.csit5930.searchengine.repository.DocumentTfidfRepository;
import com.hkust.csit5930.searchengine.service.DocumentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.hkust.csit5930.searchengine.constant.CacheConstant.*;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentTfidfRepository tfidfRepository;
    private final DocumentMetaRepository metaRepository;
    private final CacheManager cacheManager;

    public DocumentServiceImpl(DocumentTfidfRepository tfidfRepository,
                               DocumentMetaRepository metaRepository,
                               @Qualifier(DOCUMENT_CACHE_MANAGER) CacheManager cacheManager) {
        this.tfidfRepository = tfidfRepository;
        this.metaRepository = metaRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @NonNull
    public Map<Long, DocumentTfidf> getTfidfByDocumentIds(@NonNull Set<Long> documentIds) {
        Cache cache = cacheManager.getCache(DOCUMENT_TFIDF_CACHE);
        assert cache != null;
        Map<Long, DocumentTfidf> resultMap = new HashMap<>();
        Set<Long> uncachedIds = new HashSet<>();

        documentIds.forEach(id -> {
            Cache.ValueWrapper wrapper = cache.get(id);
            if (wrapper != null) {
                resultMap.put(id, (DocumentTfidf) wrapper.get());
            } else {
                uncachedIds.add(id);
            }
        });

        if (!uncachedIds.isEmpty()) {
            Map<Long, DocumentTfidf> dbResults = StreamSupport.stream(
                            tfidfRepository.findAllById(uncachedIds).spliterator(), false)
                    .collect(Collectors.toMap(DocumentTfidf::getId, Function.identity()));

            // 第三阶段：填充缓存并合并结果
            dbResults.forEach((id, entity) -> {
                cache.put(id, entity);
                resultMap.put(id, entity);
            });
        }

        return Collections.unmodifiableMap(resultMap);
    }

    @Override
    @Cacheable(cacheManager = DOCUMENT_CACHE_MANAGER, cacheNames = DOCUMENT_COUNT_CACHE, key = "'globalCount'")
    public long getDocumentCount() {
        return metaRepository.count();
    }
}
