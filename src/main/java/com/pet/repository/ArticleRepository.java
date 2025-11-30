package com.pet.repository;

import com.pet.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {
//    @Query("SELECT a FROM Article a " +
//            "WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//            "   OR a.content LIKE CONCAT('%', :keyword, '%') " +
//            "   OR LOWER(a.author.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//    Page<Article> searchArticles(@Param("keyword") String keyword, Pageable pageable);
//
//    @Query("SELECT a FROM Article a " +
//            "LEFT JOIN FETCH a.comments c " +
//            "LEFT JOIN FETCH c.user " + // load luôn user của comment
//            "WHERE a.articleId = :id")
//    Optional<Article> findArticleWithComments(@Param("id") String id);

    @Query("SELECT a FROM Article a " +
            "WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR a.content LIKE CONCAT('%', :keyword, '%') " +
            "   OR LOWER(a.author.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Article> searchArticles(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.comments c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE a.articleId = :id")
    Optional<Article> findArticleWithComments(@Param("id") String id);



}
