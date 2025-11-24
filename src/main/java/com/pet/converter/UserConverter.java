package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.Address;
import com.pet.entity.User;
import com.pet.modal.response.AddressResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConverter {

    @Autowired
    private ModelMapperConfig modelMapper;

    public UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO dto = modelMapper.getModelMapper().map(user, UserResponseDTO.class);
        if (user.getAddresses() != null) {
            List<AddressResponseDTO> addressDTOs = user.getAddresses().stream()
                    .map(this::toAddressResponseDTO)
                    .collect(Collectors.toList());
            dto.setAddresses(addressDTOs);
        } else {
            dto.setAddresses(new ArrayList<>());
        }
        return dto;
    }

    public AddressResponseDTO toAddressResponseDTO(Address address) {
        return modelMapper.getModelMapper().map(address, AddressResponseDTO.class);
    }

    public PageResponse<UserResponseDTO> toPageResponse(Page<User> userPage) {
        List<UserResponseDTO> dtos = userPage.getContent().stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());

        PageResponse<UserResponseDTO> response = new PageResponse<>();
        response.setContent(dtos);
        response.setPage(userPage.getNumber());
        response.setSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        return response;
    }
}