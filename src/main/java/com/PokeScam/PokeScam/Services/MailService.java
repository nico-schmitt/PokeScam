package com.PokeScam.PokeScam.Services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.Model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
    @Value("${server.port}")
    private int port;

    private final JavaMailSender mailSender;
    private final VerifyUserService verifyUserService;

    public MailService(JavaMailSender mailSender, VerifyUserService verifyUserService) {
        this.mailSender = mailSender;
        this.verifyUserService = verifyUserService;
    }

    public void sendVerifyEmail(User user, String to, String subject) {
        String token = verifyUserService.createVerificationToken(String.valueOf(user.getId()));
        String verifyUrl = "http://localhost:" + port + "/verify?token=" +
                URLEncoder.encode(token, StandardCharsets.UTF_8);

        String message = "Click below to verify your email:\n" + verifyUrl;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }

    public void sendUnbanEmail(User user, String to, String subject) {
        String token = verifyUserService.createVerificationToken(String.valueOf(user.getId()));
        String unbanUrl = "http://localhost:" + port + "/admin/unbanRequest?token=" +
                URLEncoder.encode(token, StandardCharsets.UTF_8);

        String message = "Click below to get unbanned:\n" + unbanUrl;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }

    public void sendHtml(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}