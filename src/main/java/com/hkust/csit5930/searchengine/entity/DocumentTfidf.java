package com.hkust.csit5930.searchengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Map;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTfidf extends EntityBase implements Serializable {
    @Column(name = "title_tfidf_vec", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, Double> titleTfidfVector;

    @Column(name = "title_mag")
    private Double titleMagnitude;

    @Column(name = "body_tfidf_vec", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, Double> bodyTfidfVector;

    @Column(name = "body_mag")
    private Double bodyMagnitude;
}
