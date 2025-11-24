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
    public void sendVaccineNotification(String toEmail, String userName, String vaccineName, String petNames, String dates) {
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
                    </div>
                    <br/>
                    <p style="color: #7f8c8d;">Trân trọng,<br/>Đội ngũ Petopia</p>
                </div>
                """, userName, vaccineName, petNames, dates);

            helper.setText(content, true);
            mailSender.send(message);
            log.info("Gửi email thành công!");

        } catch (Exception e) {
            // <--- QUAN TRỌNG: Catch Exception thay vì MessagingException để bắt cả lỗi Auth
            log.error("Gửi email thất bại (Nhưng vẫn tiếp tục xử lý): {}", e.getMessage());
            // KHÔNG throw exception lại, để chương trình chạy tiếp
        }
    }
}