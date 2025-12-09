package com.pet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.converter.ServiceConverter;
import com.pet.entity.BookingService;
import com.pet.entity.Service;
import com.pet.entity.User;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.BookingRequestDTO;
import com.pet.modal.request.ServiceRequestDTO;
import com.pet.modal.response.BookingResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ServiceResponseDTO;
import com.pet.repository.BookingServiceRepository;
import com.pet.repository.ServiceRepository;
import com.pet.repository.UserRepository;
import com.pet.service.CloudinaryService;
import com.pet.service.EmailService;
import com.pet.service.ServiceManagement;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceManagement {

    @Autowired private ServiceRepository serviceRepository;
    @Autowired private BookingServiceRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ServiceConverter serviceConverter;
    @Autowired private ModelMapper modelMapper;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmailService emailService;
    @Override
//    @Cacheable(value = "services_list", key = "#keyword + '-' + #page + '-' + #size") // Cache theo cả keyword
    public PageResponse<ServiceResponseDTO> getAllServices(String keyword, int page, int size) {
        // Gọi hàm search mới
        Page<com.pet.entity.Service> services = serviceRepository.searchServices(keyword, PageRequest.of(page, size));
        return serviceConverter.toServicePageResponse(services);
    }

    @Override
    public ServiceResponseDTO getServiceById(String id) {
        com.pet.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));
        return serviceConverter.toServiceResponseDTO(service);
    }

    @Override
    @Transactional
    @CacheEvict(value = "services_list", allEntries = true)
    public ServiceResponseDTO createService(ServiceRequestDTO request) {
        com.pet.entity.Service service = new com.pet.entity.Service();
        service.setServiceId(generateServiceId());
        modelMapper.map(request, service);
        return serviceConverter.toServiceResponseDTO(serviceRepository.save(service));
    }

    @Override
    @Transactional
    @CacheEvict(value = "services_list", allEntries = true)
    public ServiceResponseDTO updateService(String id, ServiceRequestDTO request) {
        com.pet.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

        modelMapper.map(request, service);
        return serviceConverter.toServiceResponseDTO(serviceRepository.save(service));
    }

    @Override
    @Transactional
    @CacheEvict(value = "services_list", allEntries = true)
    public void deleteService(String id) {
        if (!serviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dịch vụ không tồn tại");
        }
        serviceRepository.deleteById(id);
    }

    @Override
    public PageResponse<BookingResponseDTO> getAllBookings(String keyword, int page, int size) {
        Page<BookingService> bookings = bookingRepository.searchBookings(keyword, PageRequest.of(page, size));
        return serviceConverter.toBookingPageResponse(bookings);
    }

    // ================= USER: BOOKING SERVICES =================

    @Override
    @Transactional
    public BookingResponseDTO createBooking(String userId, BookingRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        com.pet.entity.Service serviceEntity = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

        BookingService booking = new BookingService();
        booking.setBookingServiceId(generateBookingId());
        booking.setUser(user);
        booking.setService(serviceEntity);
        booking.setAppointmentDate(request.getAppointmentDate());
        booking.setNote(request.getNote());
        booking.setQuantity(request.getQuantity());
        booking.setPriceAtPurchase(serviceEntity.getPrice());
        emailService.sendBookingNotification(request, booking);
        return serviceConverter.toBookingResponseDTO(bookingRepository.save(booking));
    }

    @Override
    public PageResponse<BookingResponseDTO> getMyBookings(String userId, int page, int size) {
        Page<BookingService> bookings = bookingRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return serviceConverter.toBookingPageResponse(bookings);
    }

    @Override
    public PageResponse<BookingResponseDTO> getAllBookings(int page, int size) {
        Page<BookingService> bookings = bookingRepository.findAll(PageRequest.of(page, size));
        return serviceConverter.toBookingPageResponse(bookings);
    }



    private String generateServiceId() {
        String lastId = serviceRepository.findLastServiceId().orElse("S000");

        try {
            int num = Integer.parseInt(lastId.substring(1));
            return String.format("S%03d", num + 1);
        } catch (NumberFormatException e) {
            return "S" + System.currentTimeMillis();
        }
    }

    private String generateBookingId() {
        String lastId = bookingRepository.findLastBookingId().orElse("BS000");

        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("PB%03d", num + 1);
        } catch (NumberFormatException e) {
            return "BS" + System.currentTimeMillis();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "services_list", allEntries = true)
    public ServiceResponseDTO createServiceWithImage(String serviceJson, MultipartFile image) throws IOException {
        // 1. Convert String JSON -> DTO
        ServiceRequestDTO request = objectMapper.readValue(serviceJson, ServiceRequestDTO.class);

        // 2. Upload ảnh nếu có
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            request.setImageUrl(imageUrl);
        }

        // 3. Map sang Entity và Lưu
        com.pet.entity.Service service = new com.pet.entity.Service();
        service.setServiceId(generateServiceId());
        modelMapper.map(request, service);

        return serviceConverter.toServiceResponseDTO(serviceRepository.save(service));
    }

    // === 2. CẬP NHẬT DỊCH VỤ (XỬ LÝ JSON + ẢNH) ===
    @Override
    @Transactional
    @CacheEvict(value = "services_list", allEntries = true)
    public ServiceResponseDTO updateServiceWithImage(String id, String serviceJson, MultipartFile image) throws IOException {
        // 1. Tìm bản ghi cũ
        com.pet.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

        // 2. Convert String JSON -> DTO
        ServiceRequestDTO request = objectMapper.readValue(serviceJson, ServiceRequestDTO.class);

        // 3. Upload ảnh mới nếu có (Nếu không gửi ảnh thì giữ nguyên ảnh cũ trong DB)
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            request.setImageUrl(imageUrl);
        } else {
            // Nếu DTO không có ảnh mới, giữ lại ảnh cũ từ Entity
            // (ModelMapper mặc định sẽ set null nếu request.imageUrl null, nên cần set lại thủ công hoặc config skip null)
            request.setImageUrl(service.getImageUrl());
        }

        // 4. Map và Lưu
        modelMapper.map(request, service);
        return serviceConverter.toServiceResponseDTO(serviceRepository.save(service));
    }
}