package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.entity.InvertedIndexBase;
import com.hkust.csit5930.searchengine.repository.BodyInvertedIndexRepository;
import com.hkust.csit5930.searchengine.repository.TitleInvertedIndexRepository;
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
    private final BodyInvertedIndexRepository bodyInvertedIndexRepository;
    private final TitleInvertedIndexRepository titleInvertedIndexRepository;
    private final CacheManager cacheManager;

    public InvertedIndexServiceImpl(BodyInvertedIndexRepository bodyInvertedIndexRepository,
                                    TitleInvertedIndexRepository titleInvertedIndexRepository,
                                    @Qualifier(INDEX_CACHE_MANAGER) CacheManager cacheManager) {
        this.bodyInvertedIndexRepository = bodyInvertedIndexRepository;
        this.titleInvertedIndexRepository = titleInvertedIndexRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @NonNull
    public Map<String, InvertedIndexBase> findBodyIndexByTermIn(@NonNull Set<String> terms) {
        return getInvertedIndexMap(terms, bodyInvertedIndexRepository::findAllByTermIn, BODY_INDEX_CACHE);
    }

    @Override
    @NonNull
    public Map<String, InvertedIndexBase> findTitleIndexByTermIn(@NonNull Set<String> terms) {
        return getInvertedIndexMap(terms, titleInvertedIndexRepository::findAllByTermIn, TITLE_INDEX_CACHE);
    }

    private <T extends InvertedIndexBase> Map<String, InvertedIndexBase> getInvertedIndexMap(Set<String> terms,
                                                                                             Converter<Set<String>, List<T>> dbQuery,
                                                                                             String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        assert cache != null;
        Map<String, InvertedIndexBase> cachedResults = new HashMap<>();
        Set<String> missingTerms = new HashSet<>();

        terms.forEach(term -> {
            Cache.ValueWrapper wrapper = cache.get(term);
            if (wrapper != null) {
                cachedResults.put(term, (InvertedIndexBase) wrapper.get());
            } else {
                missingTerms.add(term);
            }
        });

        if (!missingTerms.isEmpty()) {
            List<InvertedIndexBase> dbResults = dbQuery.convert(missingTerms).stream()
                    .map(index -> (InvertedIndexBase) index)
                    .toList();

            dbResults.forEach(index -> cache.put(index.getTerm(), index));
            dbResults.forEach(index -> cachedResults.put(index.getTerm(), index));
        }

        return cachedResults;
    }


}
