package com.pet.service;

import com.pet.modal.request.PreBookingRequestDTO;
import com.pet.modal.request.PreBookingStatusDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PreBookingResponseDTO;

public interface  PreBookingService {

    PreBookingResponseDTO createPreBooking(String userId, PreBookingRequestDTO request);

    PreBookingResponseDTO updateStatus(String bookingId, PreBookingStatusDTO request);

    PageResponse<PreBookingResponseDTO> getMyPreBookings(String userId, int page, int size);

    PageResponse<PreBookingResponseDTO> getAllPreBookings(int page, int size);

}
