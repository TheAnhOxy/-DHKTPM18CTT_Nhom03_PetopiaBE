package com.pet.controller;

import com.pet.modal.request.ArticleCommentRequestDTO;
import com.pet.modal.response.ArticleCommentResponseDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.ArticleCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/article-comments")
public class ArticleCommentController {

    @Autowired
    private ArticleCommentService articleCommentService;

    // Lấy comment theo bài viết
    @GetMapping("/article/{articleId}")
    public ResponseEntity<ApiResponse> getCommentsByArticle(@PathVariable String articleId) {
        List<ArticleCommentResponseDTO> comments = articleCommentService.getCommentsByArticle(articleId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Comments retrieved successfully")
                .data(comments)
                .build());
    }

    //  Thêm comment
    @PostMapping
    public ResponseEntity<ApiResponse> addComment(@RequestBody ArticleCommentRequestDTO request) {
        ArticleCommentResponseDTO response = articleCommentService.addComment(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Comment added successfully")
                .data(response)
                .build());
    }

    // Cập nhật comment
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse> updateComment(@PathVariable String commentId,
                                                     @RequestBody ArticleCommentRequestDTO request) {
        ArticleCommentResponseDTO response = articleCommentService.updateComment(commentId, request);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Comment updated successfully")
                .data(response)
                .build());
    }

    //  Xóa comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable String commentId,
                                                     @RequestParam String userId) {
        articleCommentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Comment deleted successfully")
                .build());
    }
}
