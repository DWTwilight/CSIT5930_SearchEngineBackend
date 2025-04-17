package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.entity.InvertedIndex;
import com.hkust.csit5930.searchengine.repository.InvertedIndexRepository;
import com.hkust.csit5930.searchengine.service.InvertedIndexService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.hkust.csit5930.searchengine.constant.CacheConstant.*;

@Service
public class InvertedIndexServiceImpl implements InvertedIndexService {
    private final InvertedIndexRepository invertedIndexRepository;
    private final CacheManager cacheManager;

    public InvertedIndexServiceImpl(
            InvertedIndexRepository invertedIndexRepository,
            @Qualifier(INDEX_CACHE_MANAGER) CacheManager cacheManager) {
        this.invertedIndexRepository = invertedIndexRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @NonNull
    public Map<String, InvertedIndex> findBodyIndexByTermIn(@NonNull Set<String> terms) {
        return getInvertedIndexMap(terms, invertedIndexRepository::findBodyIndexByTermIn, BODY_INDEX_CACHE);
    }

    @Override
    @NonNull
    public Map<String, InvertedIndex> findTitleIndexByTermIn(@NonNull Set<String> terms) {
        return getInvertedIndexMap(terms, invertedIndexRepository::findTitleIndexByTermIn, TITLE_INDEX_CACHE);
    }

    private Map<String, InvertedIndex> getInvertedIndexMap(Set<String> terms,
                                                           Converter<Set<String>, List<InvertedIndex>> dbQuery,
                                                           String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        assert cache != null;
        Map<String, InvertedIndex> cachedResults = new HashMap<>();
        Set<String> missingTerms = new HashSet<>();

        terms.forEach(term -> {
            Cache.ValueWrapper wrapper = cache.get(term);
            if (wrapper != null) {
                var cachedIndex = (InvertedIndex) wrapper.get();
                if (Objects.nonNull(cachedIndex)) {
                    cachedResults.put(term, cachedIndex);
                }
            } else {
                missingTerms.add(term);
            }
        });

        if (!missingTerms.isEmpty()) {
            dbQuery.convert(missingTerms).forEach(index -> {
                String term = index.term();
                cache.put(term, index);
                cachedResults.put(term, index);
                missingTerms.remove(term);
            });
        }

        if (!missingTerms.isEmpty()) {
            missingTerms.forEach(term -> cache.put(term, null));
        }

        return Collections.unmodifiableMap(cachedResults);
    }
}
