package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.Vaccin;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.VaccineResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VaccineConverter {
    @Autowired
    private ModelMapperConfig modelMapper;

    public VaccineResponseDTO toResponseDTO(Vaccin entity) {
        VaccineResponseDTO dto = modelMapper.getModelMapper().map(entity, VaccineResponseDTO.class);
        if (entity.getPet() != null) {
            dto.setPetName(entity.getPet().getName());
            if (entity.getPet().getImages() != null && !entity.getPet().getImages().isEmpty()) {
                dto.setPetImage(entity.getPet().getImages().iterator().next().getImageUrl());
            }
        }
        if (entity.getUser() != null) {
            dto.setOwnerName(entity.getUser().getFullName());
        }
        return dto;
    }

    public PageResponse<VaccineResponseDTO> toPageResponse(Page<Vaccin> page) {
        List<VaccineResponseDTO> list = page.getContent().stream()
                .map(this::toResponseDTO).collect(Collectors.toList());

        PageResponse<VaccineResponseDTO> response = new PageResponse<>();
        response.setContent(list);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        return response;
    }
}