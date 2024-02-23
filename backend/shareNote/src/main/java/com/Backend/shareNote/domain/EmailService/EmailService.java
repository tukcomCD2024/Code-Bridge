package com.Backend.shareNote.domain.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final MailProperties mailProperties;
    //컴파일 에러 나는거 찍어누르기
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JavaMailSender javaMailSender;
    @Async
    public boolean sendMail(EmailDTO emailDTO) {
        boolean msg = false;
        SimpleMailMessage message = new SimpleMailMessage();
        log.info("이메일 어디로 갔을까?? : " + emailDTO.getTargetMail());
        message.setTo(emailDTO.getTargetMail());
        message.setSubject("너 내 동료가 돼라");
        message.setFrom(mailProperties.getUsername());
        message.setText("너 이 링크를 누르고 내 동료가 돼라!!" + emailDTO.getLink());

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return msg;
        }
        return msg = true;

    }
}
