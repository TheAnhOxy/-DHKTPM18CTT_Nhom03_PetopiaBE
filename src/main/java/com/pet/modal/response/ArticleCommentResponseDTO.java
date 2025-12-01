package com.pet.modal.response;

import com.pet.entity.ArticleComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleCommentResponseDTO {
    public ArticleCommentResponseDTO(ArticleComment c) {
        this.commentId = c.getCommentId();
        this.content = c.getContent();
        this.createdAt = c.getCreatedAt();

        this.articleId = c.getArticle().getArticleId();

        this.userId = c.getUser().getUserId();
        this.username = c.getUser().getUsername();
    }

    private String commentId;
    private String articleId;
    private String userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
}

