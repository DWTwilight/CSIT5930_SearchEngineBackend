package com.hkust.csit5930.searchengine.entity;

import jakarta.persistence.Entity;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class BodyInvertedIndex extends InvertedIndexBase implements Serializable {

}
