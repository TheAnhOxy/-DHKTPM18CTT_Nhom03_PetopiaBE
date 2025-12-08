package com.pet.service;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO for vector search results: id + score
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoredId {
    private String id;
    private double score;
}
