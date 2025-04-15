package com.hkust.csit5930.searchengine.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheConstant {
    public static final String INDEX_CACHE_MANAGER = "indexCacheManager";
    public static final String DOCUMENT_CACHE_MANAGER = "documentCacheManager";

    public static final String BODY_INDEX_CACHE = "bodyIndexCache";
    public static final String TITLE_INDEX_CACHE = "titleIndexCache";
    public static final String BIGRAM_CACHE = "bigramCache";

    public static final String DOCUMENT_META_CACHE = "documentMetaCache";
    public static final String DOCUMENT_COUNT_CACHE = "documentCountCache";
}
