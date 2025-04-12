package com.hkust.csit5930.searchengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class InvertedIndexBase implements Serializable {
    @Id
    private Long id;
    private String term;
    @Column(name = "max_tf")
    private Long maxTermFreq;
    @Column(name = "documents", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Document> documents;
    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Document implements Serializable {
        private Long id;
        private Long titleCount;
        private List<Long> titlePos;
        private Long bodyCount;
        private List<Long> bodyPos;
    }
}
