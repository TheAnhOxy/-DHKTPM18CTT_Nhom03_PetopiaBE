package com.pet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.converter.CategoryConverter;
import com.pet.entity.Category;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.CategoryRequestDTO;
import com.pet.modal.response.CategoryResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.repository.CategoryRepository;
import com.pet.service.CategoryService;
import com.pet.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryConverter categoryConverter;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private ObjectMapper objectMapper;

    @Override
//    @Cacheable(value = "categories_list", key = "#keyword + '-' + #page + '-' + #size")
    public PageResponse<CategoryResponseDTO> getAllCategoriesOf(String keyword, int page, int size) {
        return categoryConverter.toPageResponse(
                categoryRepository.searchCategories(keyword, PageRequest.of(page, size, Sort.by("createdAt").descending()))
        );
    }

    @Override
    public CategoryResponseDTO getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));
        return categoryConverter.toDTO(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories_list", allEntries = true)
    public CategoryResponseDTO saveCategory(String categoryJson, MultipartFile image) throws IOException {
        try{
            CategoryRequestDTO request = objectMapper.readValue(categoryJson, CategoryRequestDTO.class);

            Category category;
            boolean isUpdate = request.getCategoryId() != null && !request.getCategoryId().isEmpty();
            String oldImageUrl = null; 
            if (isUpdate) {
                category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));
                oldImageUrl = category.getImageUrl();
            } else {
                if (categoryRepository.existsByName(request.getName())) {
                    throw new RuntimeException("Tên danh mục đã tồn tại");
                }
                category = new Category();
                category.setCategoryId(generateCategoryId());
            }
            categoryConverter.mapToEntity(request, category);
            if (image != null && !image.isEmpty()) {
                String url = cloudinaryService.uploadImage(image);
                category.setImageUrl(url);
            }
            else if (isUpdate) {
                if (request.getImageUrl() == null || request.getImageUrl().isEmpty()) {
                    category.setImageUrl(oldImageUrl);
                }
            }

            return categoryConverter.toDTO(categoryRepository.save(category));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories_list", allEntries = true)
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Danh mục không tồn tại");
        }
        categoryRepository.deleteById(id);
    }

    private String generateCategoryId() {
        String lastId = categoryRepository.findLastCategoryId().orElse("C000");
        try {
            int num = Integer.parseInt(lastId.substring(1));
            return String.format("C%03d", num + 1);
        } catch (Exception e) {
            return "C" + System.currentTimeMillis();
        }
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryConverter::mapToDTO)
                .toList();
    }
}
