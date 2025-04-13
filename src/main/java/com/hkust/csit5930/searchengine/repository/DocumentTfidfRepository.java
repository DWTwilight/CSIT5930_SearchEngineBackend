package com.hkust.csit5930.searchengine.repository;

import com.hkust.csit5930.searchengine.entity.DocumentTfidf;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTfidfRepository extends CrudRepository<DocumentTfidf, Long> {
}
