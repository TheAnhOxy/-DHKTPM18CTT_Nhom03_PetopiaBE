package com.pet.service;

import com.pet.modal.request.ArticleCommentRequestDTO;
import com.pet.modal.response.ArticleCommentResponseDTO;

import java.util.List;

public interface ArticleCommentService {
    ArticleCommentResponseDTO addComment(ArticleCommentRequestDTO request);
    List<ArticleCommentResponseDTO> getCommentsByArticle(String articleId);
    ArticleCommentResponseDTO updateComment(String commentId, ArticleCommentRequestDTO request);
    void deleteComment(String commentId, String userId);
}
