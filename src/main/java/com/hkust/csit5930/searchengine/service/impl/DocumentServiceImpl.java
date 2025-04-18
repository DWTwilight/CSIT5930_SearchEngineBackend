package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.entity.DocumentMeta;
import com.hkust.csit5930.searchengine.repository.DocumentMetaRepository;
import com.hkust.csit5930.searchengine.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.hkust.csit5930.searchengine.constant.CacheConstant.*;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentMetaRepository metaRepository;
    private final CacheManager cacheManager;

    public DocumentServiceImpl(DocumentMetaRepository metaRepository,
                               @Qualifier(DOCUMENT_CACHE_MANAGER) CacheManager cacheManager) {
        this.metaRepository = metaRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @NonNull
    public Map<Long, DocumentMeta> getMetaByDocumentIds(@NonNull Set<Long> documentIds) {
        Cache cache = cacheManager.getCache(DOCUMENT_META_CACHE);
        assert cache != null;
        Map<Long, DocumentMeta> resultMap = new HashMap<>();
        Set<Long> uncachedIds = new HashSet<>();

        documentIds.forEach(id -> {
            Cache.ValueWrapper wrapper = cache.get(id);
            if (wrapper != null) {
                resultMap.put(id, (DocumentMeta) wrapper.get());
            } else {
                uncachedIds.add(id);
            }
        });

        if (!uncachedIds.isEmpty()) {
            log.debug("meta cache missed, fetching {} records from db", uncachedIds.size());
            metaRepository.findAllById(uncachedIds).forEach(entity -> {
                cache.put(entity.id(), entity);
                resultMap.put(entity.id(), entity);
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
