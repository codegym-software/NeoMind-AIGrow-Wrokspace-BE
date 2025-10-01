package com.springboot.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Nullable
    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@example.com}")
    private String from;

    @Value("${app.email.subject:Bạn được mời tham gia công ty}")
    private String subject;

    @Value("${app.url:https://ai-grow.com}")
    private String appUrl;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitationEmail(String toEmail, String companyName, String inviterName) {
        if (mailSender == null) {
            System.out.println("[EMAIL] Mail sender not configured, skipping email send");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject + " " + companyName);
            String body = "Xin chào!\n\n" +
                    inviterName + " đã mời bạn tham gia công ty " + companyName + ".\n\n" +
                    "Đăng nhập tại: " + appUrl + " với email: " + toEmail + "\n\n" +
                    "AI-Grow Workspace";
            message.setText(body);
            message.setFrom(from);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send invitation: " + e.getMessage());
        }
    }
}


