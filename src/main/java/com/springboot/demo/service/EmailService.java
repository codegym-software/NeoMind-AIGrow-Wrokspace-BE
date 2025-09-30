package com.springboot.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendInvitationEmail(String toEmail, String companyName, String inviterName) {
        if (mailSender == null) {
            System.out.println("[EMAIL] Mail sender not configured, skipping email send");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("🎉 Bạn được mời tham gia công ty " + companyName);

            String emailContent = String.format("""
                Xin chào!

                Bạn đã được %s mời tham gia công ty %s trên AI-Grow Workspace.

                Để tham gia, hãy:
                1. Truy cập: http://localhost:5173
                2. Đăng nhập bằng email: %s
                3. Bắt đầu làm việc cùng team!

                Chúc bạn một ngày làm việc hiệu quả! 🚀

                ---
                AI-Grow Workspace Team
                """, inviterName, companyName, toEmail);

            message.setText(emailContent);
            message.setFrom("noreply@ai-grow.com");

            mailSender.send(message);
            System.out.println("[EMAIL] Sent invitation to: " + toEmail);
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send invitation to " + toEmail + ": " + e.getMessage());
        }
    }
}
