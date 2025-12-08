package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.Category;
import com.pet.modal.request.CategoryRequestDTO;
import com.pet.modal.response.CategoryResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryConverter {
    @Autowired
    private ModelMapperConfig modelMapper;
    @Autowired private CategoryRepository categoryRepository;
    public CategoryResponseDTO mapToDTO(Category category) {
        return modelMapper.getModelMapper().map(category, CategoryResponseDTO.class);
    }



    public CategoryResponseDTO toDTO(Category entity) {
        CategoryResponseDTO dto = modelMapper.getModelMapper().map(entity, CategoryResponseDTO.class);

        // Map Parent Info
        if (entity.getParent() != null) {
            dto.setParentId(entity.getParent().getCategoryId());
            dto.setParentName(entity.getParent().getName());
        }
        return dto;
    }

    public void mapToEntity(CategoryRequestDTO dto, Category entity) {
        modelMapper.getModelMapper().map(dto, entity);

        // Xử lý quan hệ Parent
        if (dto.getParentId() != null && !dto.getParentId().isEmpty()) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElse(null); // Hoặc throw exception nếu bắt buộc
            entity.setParent(parent);
        } else {
            entity.setParent(null); // Nếu không gửi parentId -> Là danh mục gốc
        }
    }

    public PageResponse<CategoryResponseDTO> toPageResponse(Page<Category> page) {
        List<CategoryResponseDTO> list = page.getContent().stream()
                .map(this::toDTO).collect(Collectors.toList());

        PageResponse<CategoryResponseDTO> response = new PageResponse<>();
        response.setContent(list);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        return response;
    }
}
