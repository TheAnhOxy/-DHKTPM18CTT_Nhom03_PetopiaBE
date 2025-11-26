package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.PreBooking;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PreBookingResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PreBookingConverter {
    @Autowired private ModelMapperConfig modelMapper;

    public PreBookingResponseDTO toResponseDTO(PreBooking entity) {
        PreBookingResponseDTO dto = modelMapper.getModelMapper().map(entity, PreBookingResponseDTO.class);

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getUserId());
            dto.setUserName(entity.getUser().getFullName());
            dto.setUserPhone(entity.getUser().getPhoneNumber());
        }
        if (entity.getPet() != null) {
            dto.setPetId(entity.getPet().getPetId());
            dto.setPetName(entity.getPet().getName());
            dto.setPetPrice(entity.getPet().getPrice());
            if (entity.getPet().getImages() != null && !entity.getPet().getImages().isEmpty()) {
                dto.setPetImage(entity.getPet().getImages().iterator().next().getImageUrl());
            }
        }
        return dto;
    }

    public PageResponse<PreBookingResponseDTO> toPageResponse(Page<PreBooking> page) {
        List<PreBookingResponseDTO> list = page.getContent().stream()
                .map(this::toResponseDTO).collect(Collectors.toList());
        return new PageResponse<>(list,page.getTotalElements(), page.getNumber(), page.getSize()) ;
    }
}