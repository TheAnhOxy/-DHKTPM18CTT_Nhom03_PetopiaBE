package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.Wishlist;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.WishlistResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WishlistConverter {

    @Autowired
    private ModelMapperConfig modelMapper;

    public WishlistResponseDTO toResponseDTO(Wishlist entity) {
        WishlistResponseDTO dto = modelMapper.getModelMapper().map(entity, WishlistResponseDTO.class);
        if (entity.getPet() != null) {
            dto.setPetId(entity.getPet().getPetId());
            dto.setPetName(entity.getPet().getName());
            dto.setPetPrice(entity.getPet().getPrice());
            dto.setPetStatus(entity.getPet().getStatus());
            if (entity.getPet().getImages() != null && !entity.getPet().getImages().isEmpty()) {
                dto.setPetImage(entity.getPet().getImages().iterator().next().getImageUrl());
            }
        }
        return dto;
    }

    public PageResponse<WishlistResponseDTO> toPageResponse(Page<Wishlist> page) {
        List<WishlistResponseDTO> list = page.getContent().stream()
                .map(this::toResponseDTO).collect(Collectors.toList());

        return new PageResponse<>(list,page.getTotalElements(), page.getNumber(), page.getSize());
    }
}