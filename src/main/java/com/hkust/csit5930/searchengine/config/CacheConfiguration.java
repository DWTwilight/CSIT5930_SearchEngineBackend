package com.hkust.csit5930.searchengine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

import static com.hkust.csit5930.searchengine.constant.CacheConstant.*;

@Configuration
@EnableCaching
public class CacheConfiguration {
    @Primary
    @Bean(INDEX_CACHE_MANAGER)
    public CacheManager indexCacheManager(@Value("${cache.memory.index.size:20}") int size, @Value("${cache.memory.index.ttl:30}") int ttl) {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.registerCustomCache(BODY_INDEX_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(size * 2L)
                        .expireAfterWrite(ttl, TimeUnit.MINUTES)
                        .build());

        manager.registerCustomCache(TITLE_INDEX_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(size)
                        .expireAfterWrite(ttl, TimeUnit.MINUTES)
                        .build());

        return manager;
    }

    @Bean(DOCUMENT_CACHE_MANAGER)
    public CacheManager documentCacheManager(@Value("${cache.memory.doc.size:100}") int size, @Value("${cache.memory.doc.ttl:30}") int ttl) {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.registerCustomCache(DOCUMENT_TFIDF_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(size * 2L)
                        .expireAfterWrite(ttl, TimeUnit.MINUTES)
                        .build());

        manager.registerCustomCache(DOCUMENT_META_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(size)
                        .expireAfterWrite(ttl, TimeUnit.MINUTES)
                        .build());

        manager.registerCustomCache(DOCUMENT_COUNT_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(ttl, TimeUnit.MINUTES)
                        .build());

        return manager;
    }
}
