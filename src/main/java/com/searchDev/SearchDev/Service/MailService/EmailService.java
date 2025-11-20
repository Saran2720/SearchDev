package com.searchDev.SearchDev.Service.MailService;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    EmailService(JavaMailSender javaMailSender){
        this.javaMailSender=javaMailSender;
    }
    public void sendMail(String email, String resetLink){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password reset Link");
            message.setText("Click the following link to reset your password:\n\n" + resetLink +
                    "\n\nThis link will expire in 15 minutes.");
            javaMailSender.send(message);
            System.out.println("Password reset email sent to " + email);
        } catch (Exception e) {
            System.out.println("failed to send mail"+ e.getMessage());
        }
    }
}
