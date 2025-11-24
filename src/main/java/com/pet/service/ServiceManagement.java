package com.pet.service;

import com.pet.modal.request.BookingRequestDTO;
import com.pet.modal.request.ServiceRequestDTO;
import com.pet.modal.response.BookingResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ServiceResponseDTO;

public interface ServiceManagement {

    // Admin: Service CRUD
    PageResponse<ServiceResponseDTO> getAllServices(int page, int size);
    ServiceResponseDTO getServiceById(String id);
    ServiceResponseDTO createService(ServiceRequestDTO request);
    ServiceResponseDTO updateService(String id, ServiceRequestDTO request);
    void deleteService(String id);

    // User: Booking
    BookingResponseDTO createBooking(String userId, BookingRequestDTO request);
    PageResponse<BookingResponseDTO> getMyBookings(String userId, int page, int size);

    // Admin: View all bookings
    PageResponse<BookingResponseDTO> getAllBookings(int page, int size);
    PageResponse<BookingResponseDTO> getAllBookings(String keyword, int page, int size);

}
