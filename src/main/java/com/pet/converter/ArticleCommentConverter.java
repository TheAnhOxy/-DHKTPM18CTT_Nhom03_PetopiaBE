package com.pet.converter;

import com.pet.entity.ArticleComment;
import com.pet.modal.response.ArticleCommentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ArticleCommentConverter {

    public ArticleCommentResponseDTO toResponseDTO(ArticleComment comment) {
        return ArticleCommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser() != null ? comment.getUser().getUserId() : null)
                .username(comment.getUser() != null ? comment.getUser().getUsername() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
