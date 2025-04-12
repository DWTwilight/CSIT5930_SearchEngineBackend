package com.hkust.csit5930.searchengine.rest;

import com.hkust.csit5930.searchengine.response.ResponseWrapper;
import com.hkust.csit5930.searchengine.response.data.SearchResult;
import com.hkust.csit5930.searchengine.response.meta.SearchResultMeta;
import com.hkust.csit5930.searchengine.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hkust.csit5930.searchengine.constant.ApiConstant.API_V1;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_V1 + "/s")
public class OpenSearchApiController {
    private final SearchService searchService;

    @GetMapping
    public ResponseWrapper<List<SearchResult>, SearchResultMeta> search(
            @RequestParam(name = "q") String query
    ) {
        var results = searchService.search(query);
        return new ResponseWrapper<>(results, new SearchResultMeta(results.size()));
    }
}
