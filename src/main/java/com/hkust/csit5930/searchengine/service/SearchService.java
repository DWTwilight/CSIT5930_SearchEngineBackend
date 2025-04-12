package com.hkust.csit5930.searchengine.service;

import com.hkust.csit5930.searchengine.response.data.SearchResult;
import org.springframework.lang.NonNull;

import java.util.List;

public interface SearchService {
    @NonNull
    List<SearchResult> search(@NonNull String query);
}
