package com.plakhotnikov.cloud_storage_engine.security.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


/**
 * Сервис для отправки email-сообщений пользователям.
 * Поддерживает отправку писем для подтверждения email и сброса пароля.
 *
 * @see JavaMailSender
 */
@Service
@RequiredArgsConstructor
public class EmailService {
    // TODO: 11.02.2025 не хардкодить адрес, задавать из property
    private final JavaMailSender mailSender;

    /**
     * Отправляет email с токеном для сброса пароля.
     *
     * @param to Email получателя.
     * @param token Токен сброса пароля.
     */
    public void sendResetPasswordEmail(String to, String token) {
        String subject = "Смена пароля";
//        String confirmationUrl = "http://localhost:8080/auth/reset-password" + token;
        String content = "<p>Здравствуйте,</p>"
                + "<p>token= " + token + "</p>";
// TODO: 12.02.2025 ссылку на get и затем там button на post 
        sendHtmlEmail(to, subject, content);
    }

    /**
     * Отправляет email с ссылкой для подтверждения регистрации.
     *
     * @param to Email получателя.
     * @param token Токен подтверждения.
     */
    public void sendVerificationEmail(String to, String token) {
        String subject = "Подтверждение регистрации";
        String confirmationUrl = "http://localhost:8080/auth/verify-email?token=" + token;
        String content = "<p>Здравствуйте,</p>"
                + "<p>Для подтверждения регистрации перейдите по ссылке:</p>"
                + "<p><a href=\"" + confirmationUrl + "\">Подтвердить Email</a></p>";

        sendHtmlEmail(to, subject, content);
    }


    /**
     * Отправляет HTML-email сообщение пользователю.
     *
     * @param to Email получателя.
     * @param subject Тема письма.
     * @param content HTML-контент письма.
     * @throws RuntimeException если произошла ошибка при отправке email.
     */
    private void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка отправки email", e);
        }
    }
}
