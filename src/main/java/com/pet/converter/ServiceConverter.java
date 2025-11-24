package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.BookingService;
import com.pet.entity.Service;
import com.pet.modal.response.BookingResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ServiceResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceConverter {

    @Autowired
    private ModelMapperConfig modelMapper;

    public ServiceResponseDTO toServiceResponseDTO(Service service) {
        return modelMapper.getModelMapper().map(service, ServiceResponseDTO.class);
    }

    public PageResponse<ServiceResponseDTO> toServicePageResponse(Page<Service> page) {
        List<ServiceResponseDTO> list = page.getContent().stream()
                .map(this::toServiceResponseDTO).collect(Collectors.toList());
        return new PageResponse<>(list,page.getTotalElements(), page.getNumber(), page.getSize());
    }

    //  Booking Mapping
    public BookingResponseDTO toBookingResponseDTO(BookingService booking) {
        BookingResponseDTO dto = modelMapper.getModelMapper().map(booking, BookingResponseDTO.class);
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getUserId());
            dto.setUserName(booking.getUser().getFullName());
        }
        if (booking.getService() != null) {
            dto.setServiceId(booking.getService().getServiceId());
            dto.setServiceName(booking.getService().getName());
            dto.setServiceImage(booking.getService().getImageUrl());
        }
        dto.setTotalAmount(booking.getPriceAtPurchase() * booking.getQuantity());

        return dto;
    }

    public PageResponse<BookingResponseDTO> toBookingPageResponse(Page<BookingService> page) {
        List<BookingResponseDTO> list = page.getContent().stream()
                .map(this::toBookingResponseDTO).collect(Collectors.toList());
        return new PageResponse<>(list,page.getTotalElements(), page.getNumber(), page.getSize());
    }
}