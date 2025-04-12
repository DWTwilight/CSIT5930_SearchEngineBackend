package com.hkust.csit5930.searchengine.repository;

import com.hkust.csit5930.searchengine.entity.TitleInvertedIndex;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface TitleInvertedIndexRepository extends CrudRepository<TitleInvertedIndex, Long> {
    @Transactional(readOnly = true)
    List<TitleInvertedIndex> findAllByTermIn(Set<String> terms);
}
