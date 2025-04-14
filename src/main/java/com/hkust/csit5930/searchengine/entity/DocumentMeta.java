package com.hkust.csit5930.searchengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMeta extends EntityBase {
    private String title;

    private String url;

    private String lastModified;

    private Long size;

    @Column(name = "freq_words", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Long> frequentWords;

    @Column(name = "parent_links", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> parentLinks;

    @Column(name = "child_links", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> childLinks;

    // for normalization
    private Long maxTitleTf;

    // for normalization
    private Long maxBodyTf;

    private Double pageRank;
}
