package com.hkust.csit5930.searchengine.entity;

import java.util.List;
import java.util.Map;

public record DocumentMeta(long id, String title, String url, String lastModified, long size,
                           Map<String, Long> frequentWords, List<String> parentLinks, List<String> childLinks,
                           long maxTitleTf, long maxBodyTf, double pageRank) implements Identifiable {
}
