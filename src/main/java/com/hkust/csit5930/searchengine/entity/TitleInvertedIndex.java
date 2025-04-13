package com.hkust.csit5930.searchengine.entity;

import jakarta.persistence.Entity;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TitleInvertedIndex extends InvertedIndexBase implements Serializable {

}
