package com.pet.repository;

import com.pet.entity.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, String> {
    List<ArticleComment> findByArticle_ArticleId(String articleId);
}
