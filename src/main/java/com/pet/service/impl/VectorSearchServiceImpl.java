package com.pet.service.impl;



import com.pet.service.ScoredId;
import com.pet.service.VectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation when there's no vector DB configured.
 * - searchIdsByText returns empty list (or a simple keyword fallback if desired)
 * - searchIdsWithScore returns empty list
 *
 * You can enhance this class to do a lightweight keyword matching fallback if you prefer.
 */
@Service
@Slf4j
public class VectorSearchServiceImpl implements VectorSearchService {

    @Override
    public List<String> searchIdsByText(String domain, String text, int topK) {
        // Default fallback: no vector DB, return empty result so system will use DB queries
        log.debug("VectorSearchFallback: searchIdsByText called for domain={}, text={}, topK={}", domain, text, topK);
        return Collections.emptyList();
    }

    @Override
    public List<ScoredId> searchIdsWithScore(String domain, String text, int topK) {
        log.debug("VectorSearchFallback: searchIdsWithScore called for domain={}, text={}, topK={}", domain, text, topK);
        // Return empty — AiServiceHybridImpl handles fallback to repository search.
        return Collections.emptyList();
    }

    // Optional helper: a simple keyword-to-id mapping could be implemented here later.
}
