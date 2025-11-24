package com.pet.service;

import com.pet.modal.request.ArticleRequestDTO;
import com.pet.modal.response.ArticleResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ArticleService {
    ArticleResponseDTO createArticle(ArticleRequestDTO request);
    ArticleResponseDTO getArticleById(String id);
    List<ArticleResponseDTO> getAllArticles();
    ArticleResponseDTO updateArticle(String id, ArticleRequestDTO request);
    void deleteArticle(String id, String authorId);
    Page<ArticleResponseDTO> getAllArticlesPaged(int page, int size, String sortBy, String sortDir, String keyword);


}
