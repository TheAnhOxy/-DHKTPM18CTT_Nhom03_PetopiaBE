package com.pet.modal.response;

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
    private String commentId;
    private String articleId;
    private String articleTitle;
    private String userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
}
