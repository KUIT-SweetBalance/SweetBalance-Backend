package com.sweetbalance.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTemporaryPasswordMail(String to, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[SweetBalance] 임시 비밀번호 안내");
        message.setText("임시 비밀번호는 [" + tempPassword + "] 입니다. 해당 비밀번호로 로그인 해주세요.");
        mailSender.send(message);
    }

    public void sendEmailVerificationCodeMail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[SweetBalance] 이메일 인증코드 안내");
        message.setText("이메일 인증코드는 " + verificationCode + " 입니다.\n" + "해당 코드를 입력하여 이메일 인증을 완료해주세요.");
        mailSender.send(message);
    }
}
