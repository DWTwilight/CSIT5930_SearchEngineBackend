package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.entity.DocumentMeta;
import com.hkust.csit5930.searchengine.entity.DocumentTfidf;
import com.hkust.csit5930.searchengine.entity.Identifiable;
import com.hkust.csit5930.searchengine.repository.DocumentMetaRepository;
import com.hkust.csit5930.searchengine.repository.DocumentTfidfRepository;
import com.hkust.csit5930.searchengine.service.DocumentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

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
        return getCachedOrDBResults(documentIds, DOCUMENT_TFIDF_CACHE, tfidfRepository::findAllById);
    }

    @Override
    @NonNull
    public Map<Long, DocumentMeta> getMetaByDocumentIds(@NonNull Set<Long> documentIds) {
        return getCachedOrDBResults(documentIds, DOCUMENT_META_CACHE, metaRepository::findAllById);
    }

    @Override
    @Cacheable(cacheManager = DOCUMENT_CACHE_MANAGER, cacheNames = DOCUMENT_COUNT_CACHE, key = "'globalCount'")
    public long getDocumentCount() {
        return metaRepository.count();
    }


    @SuppressWarnings("unchecked")
    private <T extends Identifiable> Map<Long, T> getCachedOrDBResults(Set<Long> documentIds,
                                                                       String cacheName,
                                                                       Converter<Set<Long>, List<T>> dbQuery) {
        Cache cache = cacheManager.getCache(cacheName);
        assert cache != null;
        Map<Long, T> resultMap = new HashMap<>();
        Set<Long> uncachedIds = new HashSet<>();

        documentIds.forEach(id -> {
            Cache.ValueWrapper wrapper = cache.get(id);
            if (wrapper != null) {
                resultMap.put(id, (T) wrapper.get());
            } else {
                uncachedIds.add(id);
            }
        });

        if (!uncachedIds.isEmpty()) {
            dbQuery.convert(uncachedIds).forEach(entity -> {
                cache.put(entity.id(), entity);
                resultMap.put(entity.id(), entity);
            });
        }

        return Collections.unmodifiableMap(resultMap);
    }
}
