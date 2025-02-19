package com.example.cafe.domain.member.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String SENDER_EMAIL;


    public MimeMessage createMail(String recipientEmail, String code) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom(SENDER_EMAIL);
        message.setRecipients(MimeMessage.RecipientType.TO, recipientEmail);
        message.setSubject("이메일 인증");
        String body = "<h3>요청하신 인증 번호입니다.</h3>"
                + "<h1>" + code + "</h1>"
                + "<h3>감사합니다.</h3>";
        message.setText(body, "UTF-8", "html");
        return message;
    }

    // 메일 발송 (인증 코드를 매개변수로 받아 이메일 전송)
    public void sendSimpleMessage(String recipientEmail, String code) throws MessagingException {
        MimeMessage message = createMail(recipientEmail, code);
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("메일 발송 중 오류가 발생했습니다.");
        }
    }
}
