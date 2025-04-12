package com.hkust.csit5930.searchengine.service.impl;

import com.hkust.csit5930.searchengine.entity.InvertedIndexBase;
import com.hkust.csit5930.searchengine.repository.BodyInvertedIndexRepository;
import com.hkust.csit5930.searchengine.repository.TitleInvertedIndexRepository;
import com.hkust.csit5930.searchengine.service.InvertedIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InvertedIndexServiceImpl implements InvertedIndexService {
    private final BodyInvertedIndexRepository bodyInvertedIndexRepository;
    private final TitleInvertedIndexRepository titleInvertedIndexRepository;

    @Override
    @NonNull
    public List<InvertedIndexBase> findBodyIndexByTermIn(@NonNull Set<String> terms) {
        return bodyInvertedIndexRepository.findAllByTermIn(terms).stream().map(index -> (InvertedIndexBase) index).toList();
    }

    @Override
    @NonNull
    public List<InvertedIndexBase> findTitleIndexByTermIn(@NonNull Set<String> terms) {
        return titleInvertedIndexRepository.findAllByTermIn(terms).stream().map(index -> (InvertedIndexBase) index).toList();
    }
}
