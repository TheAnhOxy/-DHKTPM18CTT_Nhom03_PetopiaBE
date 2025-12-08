package com.pet.service;




import java.util.List;

/**
 * Vector search abstraction.
 *
 * Implement this for your embedding/vector DB.
 * Fallback implementation (no vector DB) is provided (VectorSearchFallbackImpl).
 */
public interface VectorSearchService {

    /**
     * Return ranked list of ids (no scores). Kept for backward compatibility.
     */
    List<String> searchIdsByText(String domain, String text, int topK);

    /**
     * Preferred method: return ids with similarity scores (0.0 - 1.0).
     * If your vector provider doesn't provide scores normalized to [0,1], you can normalize them.
     */
    List<ScoredId> searchIdsWithScore(String domain, String text, int topK);
}
