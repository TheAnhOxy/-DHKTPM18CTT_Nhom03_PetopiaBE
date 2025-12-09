package com.pet.controller.admin;

import com.pet.modal.response.ApiResponse;
import com.pet.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(categoryService.getAllCategoriesOf(keyword, page, size))
                .build());
    }

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> saveCategory(
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.builder()
                            .status(201)
                            .message("Lưu danh mục thành công")
                            .data(categoryService.saveCategory(categoryJson, image))
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .status(400)
                            .message("Lỗi: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Đã xóa danh mục")
                .build());
    }
}