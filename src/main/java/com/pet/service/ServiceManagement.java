package com.pet.service;

import com.pet.modal.request.BookingRequestDTO;
import com.pet.modal.request.ServiceRequestDTO;
import com.pet.modal.response.BookingResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ServiceResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ServiceManagement {

    // Admin: Service CRUD
    PageResponse<ServiceResponseDTO> getAllServices(String keyword,int page, int size);
    ServiceResponseDTO getServiceById(String id);
    ServiceResponseDTO createService(ServiceRequestDTO request);
    ServiceResponseDTO updateService(String id, ServiceRequestDTO request);
    void deleteService(String id);
    ServiceResponseDTO createServiceWithImage(String serviceJson, MultipartFile image) throws IOException;

    ServiceResponseDTO updateServiceWithImage(String id, String serviceJson, MultipartFile image) throws IOException;
    // User: Booking
    BookingResponseDTO createBooking(String userId, BookingRequestDTO request);
    PageResponse<BookingResponseDTO> getMyBookings(String userId, int page, int size);

    // Admin: View all bookings
    PageResponse<BookingResponseDTO> getAllBookings(int page, int size);
    PageResponse<BookingResponseDTO> getAllBookings(String keyword, int page, int size);

}
