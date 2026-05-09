
    package com.carcaddy.service.impl;

import com.carcaddy.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendAccountDeactivationEmail(String toEmail, String employeeName) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("CarCaddy | Account Deactivated");

        message.setText(
                "Dear " + employeeName + ",\n\n" +
                "Your account has been deactivated as your employment period has expired.\n\n" +
                "Please contact the administrator for further assistance.\n\n" +
                "Regards,\nCarCaddy Team"
        );

        mailSender.send(message);
    }
}




