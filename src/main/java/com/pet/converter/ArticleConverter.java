package com.pet.converter;

import com.pet.entity.Article;
import com.pet.modal.response.ArticleCommentResponseDTO;
import com.pet.modal.response.ArticleResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticleConverter {

    @Autowired
    private ArticleCommentConverter articleCommentConverter;

    public ArticleResponseDTO toResponseDTO(Article article) {
        List<ArticleCommentResponseDTO> commentDTOs = null;

        if (article.getComments() != null && !article.getComments().isEmpty()) {
            commentDTOs = article.getComments().stream()
                    .map(articleCommentConverter::toResponseDTO)
                    .collect(Collectors.toList());
        }

        return ArticleResponseDTO.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .content(article.getContent())
                .imageUrl(article.getImageUrl())
                .authorId(article.getAuthor() != null ? article.getAuthor().getUserId() : null)
                .authorName(article.getAuthor() != null ? article.getAuthor().getUsername() : null)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .comments(commentDTOs)
                .build();
    }
}
