package com.pet.service;

import com.pet.entity.PreBooking;
import com.pet.enums.BookingStatus;
import com.pet.repository.PreBookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingCleanupService {

    @Autowired
    private PreBookingRepository preBookingRepository;

    @Autowired
    private EmailService emailService;

    // Chạy mỗi 1 phút (60000ms)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelExpiredBookings() {
        log.info("--- Bắt đầu quét các đơn đặt trước quá hạn ---");

        // Cấu hình thời gian hết hạn: 2 phút để test
//        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(2);

        // Hết hạn sau 24 giờ
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        List<PreBooking> expiredBookings = preBookingRepository.findExpiredConfirmedBookings(cutoffTime);

        for (PreBooking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            preBookingRepository.save(booking);

            //  Gửi mail báo khách
            log.info("Đã hủy tự động đơn: {}", booking.getBookingId());
            if (booking.getUser().getEmail() != null) {
                emailService.sendPreBookingStatusNotification(
                        booking.getUser().getEmail(),
                        booking.getUser().getFullName(),
                        booking.getPet().getName(),
                        "CANCELLED",
                        "Đơn hàng bị hủy tự động do quá hạn xác nhận/không đến nhận thú cưng."
                );
            }
        }

        if (!expiredBookings.isEmpty()) {
            log.info("Đã hủy tổng cộng: {} đơn", expiredBookings.size());
        }
    }
}