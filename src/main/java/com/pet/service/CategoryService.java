package com.pet.service;

import com.pet.modal.response.CategoryResponseDTO;
import com.pet.modal.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAllCategories();
    public PageResponse<CategoryResponseDTO> getAllCategoriesOf(String keyword, int page, int size);
    CategoryResponseDTO getCategoryById(String id);
    public CategoryResponseDTO saveCategory(String categoryJson, MultipartFile image) throws IOException;
    public void deleteCategory(String id);

}
