package com.pet.service.impl;

import com.pet.converter.PreBookingConverter;
import com.pet.entity.Notification;
import com.pet.entity.Pet;
import com.pet.entity.PreBooking;
import com.pet.entity.User;
import com.pet.enums.BookingStatus;
import com.pet.enums.NotificationType;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.PreBookingRequestDTO;
import com.pet.modal.request.PreBookingStatusDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PreBookingResponseDTO;
import com.pet.repository.NotificationRepository;
import com.pet.repository.PetRepository;
import com.pet.repository.PreBookingRepository;
import com.pet.repository.UserRepository;
import com.pet.service.EmailService;
import com.pet.service.PreBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PreBookingServiceImpl implements PreBookingService {

    @Autowired private PreBookingRepository preBookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private PreBookingConverter preBookingConverter;
    @Autowired private EmailService emailService;

    //  User tạo yêu cầu đặt trước
    @Transactional
    @Override
    public PreBookingResponseDTO createPreBooking(String userId, PreBookingRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));

        PreBooking preBooking = new PreBooking();
        preBooking.setBookingId(generatePreBookingId());
        preBooking.setUser(user);
        preBooking.setPet(pet);
        preBooking.setDepositAmount(request.getDepositAmount());
        preBooking.setExpectedDate(request.getExpectedDate());
        preBooking.setStatus(BookingStatus.PENDING); // Mặc định là chờ duyệt

        PreBooking saved = preBookingRepository.save(preBooking);
        return preBookingConverter.toResponseDTO(saved);
    }

    //  Admin cập nhật trạng thái (Duyệt/Hủy) -> Gửi Mail & Noti
    @Transactional
    @Override
    public PreBookingResponseDTO updateStatus(String bookingId, PreBookingStatusDTO request) {
        PreBooking preBooking = preBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Pre-booking not found"));

        // Cập nhật trạng thái
        preBooking.setStatus(request.getStatus());
        PreBooking saved = preBookingRepository.save(preBooking);

        // --- GỬI EMAIL ---
        if (saved.getUser().getEmail() != null) {
            emailService.sendPreBookingStatusNotification(
                    saved.getUser().getEmail(),
                    saved.getUser().getFullName(),
                    saved.getPet().getName(),
                    saved.getStatus().name(),
                    request.getNote()
            );
        }

        // --- TẠO NOTIFICATION (In-App)
        createNotification(saved, request.getNote());
        return preBookingConverter.toResponseDTO(saved);
    }

    //  Lấy danh sách cho User
    @Override
    public PageResponse<PreBookingResponseDTO> getMyPreBookings(String userId, int page, int size) {
        Page<PreBooking> bookings = preBookingRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return preBookingConverter.toPageResponse(bookings);
    }

    //  Lấy tất cả cho Admin
    @Override
    public PageResponse<PreBookingResponseDTO> getAllPreBookings(int page, int size) {
        return preBookingConverter.toPageResponse(preBookingRepository.findAll(PageRequest.of(page, size)));
    }

    private String generatePreBookingId() {
        String lastId = preBookingRepository.findLastBookingId().orElse("PB000");
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("PB%03d", num + 1);
        } catch (NumberFormatException e) {
            return "PB" + System.currentTimeMillis();
        }
    }

    private void createNotification(PreBooking booking, String note) {
        Notification noti = new Notification();
        noti.setNotificationId("N" + System.currentTimeMillis());
        noti.setUser(booking.getUser());
        noti.setTypeNote(NotificationType.ORDER_UPDATE);
        noti.setIsRead(false);

        String statusText = booking.getStatus() == BookingStatus.CONFIRMED ? "thành công" : "đã bị hủy";
        noti.setTitle("Cập nhật đặt trước thú cưng");
        noti.setContent("Yêu cầu đặt trước bé " + booking.getPet().getName() + " của bạn " + statusText + ". Ghi chú: " + (note != null ? note : ""));

        notificationRepository.save(noti);
    }
}