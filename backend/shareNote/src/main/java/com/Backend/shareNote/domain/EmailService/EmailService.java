package com.Backend.shareNote.domain.EmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(emailDTO.getTargetMail());
            helper.setSubject("sharenote 초대장");
            helper.setFrom("your-email@example.com"); // 이메일 발신자 주소
            String htmlMsg = """
    <div style="font-family: 'Arial', sans-serif; color: #333;">
        <h2>안녕하세요&nbsp; """ + emailDTO.getNickname() + """
        님으로부터 도착한 초대장입니다.</h2>
        <p>아래의 버튼을 눌러 노트를 작성해주세요!</p>
        <div style="margin: 20px 0;">
            <a href='""" + emailDTO.getLink() + """
        ' style="background-color: #4CAF50; color: white; padding: 14px 20px; text-align: center; text-decoration: none; display: inline-block; border-radius: 5px;">초대장</a>
        </div>

    </p>
    </div>
    """;

            helper.setText(htmlMsg, true); // HTML 콘텐츠로 이메일 설정


            javaMailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

//        boolean msg = false;
//        SimpleMailMessage message = new SimpleMailMessage();
//        log.info("이메일 어디로 갔을까?? : " + emailDTO.getTargetMail());
//        message.setTo(emailDTO.getTargetMail());
//        message.setSubject("너 내 동료가 돼라");
//        message.setFrom(mailProperties.getUsername());
//        message.setText("너 이 링크를 누르고 내 동료가 돼라!! from " +emailDTO.getNickname() + " " + emailDTO.getLink());
//
//        try {
//            javaMailSender.send(message);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return msg;
//        }
//        return msg = true;

//    }
//}
