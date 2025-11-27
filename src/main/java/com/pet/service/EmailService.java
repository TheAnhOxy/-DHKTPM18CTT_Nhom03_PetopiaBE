package com.pet.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender ;

    @Async
    public void sendVaccineNotification(String toEmail, String userName, String vaccineName,
                                        String petNames, String dateRange, String note) {
        try {
            log.info("Đang bắt đầu gửi email tới: {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[Petopia] Thông báo lịch tiêm phòng mới");

            String content = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #2c3e50;">Xin chào %s,</h2>
                    <p>Petopia vừa cập nhật lịch tiêm phòng mới cho thú cưng của bạn.</p>
                    <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px;">
                        <p><strong>Loại vắc xin:</strong> %s</p>
                        <p><strong>Thú cưng:</strong> %s</p>
                        <p><strong>Thời gian dự kiến:</strong> %s</p>
                        <p><strong>Ghi chú:</strong> %s</p>
                    </div>
                    <br/>
                    <p style="color: #7f8c8d;">Trân trọng,<br/>Đội ngũ Petopia</p>
                </div>
                """, userName, vaccineName, petNames, dateRange, (note != null ? note : "Không có"));

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Gửi email thành công!");

        } catch (Exception e) {
            log.error("Gửi email thất bại: {}", e.getMessage());
        }
    }

    @Async
    public void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            log.info("Đang gửi email tới: {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Gửi email thành công!");

        } catch (Exception e) {
            log.error("Gửi email thất bại: {}", e.getMessage());
        }
    }

    @Async
    public void sendPreBookingStatusNotification(String toEmail, String userName, String petName, String status, String note) {
        try {
            log.info("Gửi email thông báo trạng thái đặt trước tới: {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);

            String subject = status.equals("CONFIRMED")
                    ? "[Petopia] Xác nhận đặt trước thú cưng thành công"
                    : "[Petopia] Thông báo hủy yêu cầu đặt trước";

            helper.setSubject(subject);

            String color = status.equals("CONFIRMED") ? "#27ae60" : "#c0392b";
            String statusvn = status.equals("CONFIRMED") ? "ĐÃ XÁC NHẬN" : "ĐÃ HỦY";

            String content = String.format("""
                    <div style="font-family: Arial, sans-serif; padding: 20px;">
                        <h2 style="color: #2c3e50;">Xin chào %s,</h2>
                        <p>Yêu cầu đặt trước thú cưng <strong>%s</strong> của bạn đã được xử lý.</p>
                        <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; border-left: 5px solid %s;">
                            <p><strong>Trạng thái:</strong> <span style="color: %s; font-weight: bold;">%s</span></p>
                            <p><strong>Ghi chú từ cửa hàng:</strong> %s</p>
                        </div>
                        <p>Nếu có thắc mắc, vui lòng liên hệ hotline.</p>
                        <br/>
                        <p style="color: #7f8c8d;">Trân trọng,<br/>Đội ngũ Petopia</p>
                    </div>
                    """, userName, petName, color, color, statusvn, (note != null ? note : "Không có"));

            helper.setText(content, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Lỗi gửi mail PreBooking: {}", e.getMessage());
        }



    }

}