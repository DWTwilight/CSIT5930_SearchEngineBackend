package com.hkust.csit5930.searchengine.repository;

import com.hkust.csit5930.searchengine.entity.BodyInvertedIndex;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface BodyInvertedIndexRepository extends CrudRepository<BodyInvertedIndex, Long> {
    @Transactional(readOnly = true)
    List<BodyInvertedIndex> findAllByTermIn(Set<String> terms);
}
