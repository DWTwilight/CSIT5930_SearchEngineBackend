package com.hkust.csit5930.searchengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTfidf {
    @Id
    private Long id;

    @Column(name = "title_tfidf_vec", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, Double> titleTfidfVector;

    @Column(name = "body_tfidf_vec", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, Double> bodyTfidfVector;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
