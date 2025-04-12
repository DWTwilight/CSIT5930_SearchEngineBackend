package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.response.data.SearchResult;
import com.hkust.csit5930.searchengine.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    @Override
    @NonNull
    public List<SearchResult> search(@NonNull String query) {
        return List.of();
    }
}
