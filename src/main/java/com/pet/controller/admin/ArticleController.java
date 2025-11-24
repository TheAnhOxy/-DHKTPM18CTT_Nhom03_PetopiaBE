package com.pet.controller.admin;

import com.pet.modal.request.ArticleRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.modal.response.ArticleResponseDTO;
import com.pet.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // Tạo bài viết (chỉ ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse> createArticle(@RequestBody ArticleRequestDTO request) {
        try {
            ArticleResponseDTO response = articleService.createArticle(request);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .message("Article created successfully")
                    .data(response)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(ApiResponse.builder()
                    .status(403)
                    .message(e.getMessage())
                    .build());
        }
    }

    // Xem chi tiết bài viết (all user)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getArticleById(@PathVariable String id) {
        ArticleResponseDTO response = articleService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Article retrieved successfully")
                .data(response)
                .build());
    }


    //  Xem tất cả bài viết (all)
    @GetMapping
    public ResponseEntity<ApiResponse> getAllArticles() {
        List<ArticleResponseDTO> responses = articleService.getAllArticles();
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Articles retrieved successfully")
                .data(responses)
                .build());
    }

    // Cập nhật bài viết (for ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateArticle(@PathVariable String id,
                                                     @RequestBody ArticleRequestDTO request) {
        try {
            ArticleResponseDTO response = articleService.updateArticle(id, request);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .message("Article updated successfully")
                    .data(response)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(ApiResponse.builder()
                    .status(403)
                    .message(e.getMessage())
                    .build());
        }
    }

    //  Xóa bài viết (for ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteArticle(@PathVariable String id,
                                                     @RequestBody Map<String, String> body) {
        try {
            String authorId = body.get("authorId");
            articleService.deleteArticle(id, authorId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .message("Article deleted successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(ApiResponse.builder()
                    .status(403)
                    .message(e.getMessage())
                    .build());
        }
    }
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse> getAllArticlesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {

        Page<ArticleResponseDTO> responses = articleService.getAllArticlesPaged(page, size, sortBy, sortDir, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("articles", responses.getContent());
        result.put("currentPage", responses.getNumber());
        result.put("totalItems", responses.getTotalElements());
        result.put("totalPages", responses.getTotalPages());

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Articles retrieved successfully with pagination and search")
                .data(result)
                .build());
    }




}
