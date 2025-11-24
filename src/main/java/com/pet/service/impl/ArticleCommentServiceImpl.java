package com.pet.service.impl;

import com.pet.converter.ArticleCommentConverter;
import com.pet.entity.Article;
import com.pet.entity.ArticleComment;
import com.pet.entity.User;
import com.pet.enums.UserRole;
import com.pet.modal.request.ArticleCommentRequestDTO;
import com.pet.modal.response.ArticleCommentResponseDTO;
import com.pet.repository.ArticleCommentRepository;
import com.pet.repository.ArticleRepository;
import com.pet.repository.UserRepository;
import com.pet.service.ArticleCommentService;
import com.pet.utils.IdGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleCommentServiceImpl implements ArticleCommentService {

    @Autowired
    private ArticleCommentRepository articleCommentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleCommentConverter converter;

    @Override
    @Transactional
    public ArticleCommentResponseDTO addComment(ArticleCommentRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new RuntimeException("Article not found"));

        ArticleComment comment = new ArticleComment();
        comment.setCommentId(IdGeneratorUtil.generateCommentId());
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(request.getContent());

        return converter.toResponseDTO(articleCommentRepository.save(comment));
    }

    @Override
    public List<ArticleCommentResponseDTO> getCommentsByArticle(String articleId) {
        return articleCommentRepository.findByArticle_ArticleId(articleId)
                .stream()
                .map(converter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ArticleCommentResponseDTO updateComment(String commentId, ArticleCommentRequestDTO request) {
        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ❌ Admin không được phép sửa comment của người khác
        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied: You can only edit your own comment.");
        }


        comment.setContent(request.getContent());
        return converter.toResponseDTO(articleCommentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, String userId) {
        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!comment.getUser().getUserId().equals(user.getUserId())
                && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Access denied: You can only delete your own comment or be ADMIN.");
        }
        articleCommentRepository.delete(comment);
    }
}
