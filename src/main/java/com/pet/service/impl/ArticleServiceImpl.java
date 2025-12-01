package com.pet.service.impl;

import com.pet.converter.ArticleConverter;
import com.pet.entity.Article;
import com.pet.entity.User;
import com.pet.enums.UserRole;
import com.pet.modal.request.ArticleRequestDTO;
import com.pet.modal.response.ArticleResponseDTO;
import com.pet.repository.ArticleRepository;
import com.pet.repository.UserRepository;
import com.pet.service.ArticleService;
import com.pet.utils.IdGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleConverter articleConverter;

    /**
     * ✅ Chỉ ADMIN mới được tạo bài viết.
     */
    @Override
    @Transactional
    public ArticleResponseDTO createArticle(ArticleRequestDTO request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        if (author.getRole() == null || !author.getRole().equals(UserRole.ADMIN)) {
            throw new RuntimeException("Access denied: Only ADMIN can create articles.");
        }

        Article article = new Article();
        article.setArticleId(IdGeneratorUtil.generateArticleId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setImageUrl(request.getImageUrl());
        article.setAuthor(author);

        return articleConverter.toResponseDTO(articleRepository.save(article));
    }

    /**
     * ✅ Ai cũng có thể xem chi tiết bài viết.
     */
    @Override
    public ArticleResponseDTO getArticleById(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        return articleConverter.toResponseDTO(article);
    }


    /**
     * ✅ Ai cũng có thể xem danh sách bài viết.
     */
    @Override
    public List<ArticleResponseDTO> getAllArticles() {
        return articleRepository.findAll()
                .stream()
                .map(articleConverter::toResponseDTO)
                .collect(Collectors.toList());
    }


    /**
     * ✅ Chỉ ADMIN mới được cập nhật bài viết.
     */
    @Override
    @Transactional
    public ArticleResponseDTO updateArticle(String id, ArticleRequestDTO request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        if (author.getRole() == null || !author.getRole().equals(UserRole.ADMIN)) {
            throw new RuntimeException("Access denied: Only ADMIN can update articles.");
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setImageUrl(request.getImageUrl());
        article.setAuthor(author);

        return articleConverter.toResponseDTO(articleRepository.save(article));
    }

    /**
     * ✅ Chỉ ADMIN mới được xóa bài viết.
     */
    @Override
    @Transactional
    public void deleteArticle(String id, String authorId) {
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == null || !user.getRole().equals(UserRole.ADMIN)) {
            throw new RuntimeException("Access denied: Only ADMIN can delete articles.");
        }

        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("Article not found");
        }

        articleRepository.deleteById(id);
    }
    @Override
    public Page<ArticleResponseDTO> getAllArticlesPaged(int page, int size, String sortBy, String sortDir, String keyword) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Article> articlePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            articlePage = articleRepository.searchArticles(keyword.trim(), pageable);
        } else {
            articlePage = articleRepository.findAll(pageable);
        }

        return articlePage.map(articleConverter::toResponseDTO);
    }



}
