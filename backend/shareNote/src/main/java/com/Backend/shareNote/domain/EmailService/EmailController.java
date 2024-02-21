package com.Backend.shareNote.domain.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmailController {
    private final EmailService emailService;
    @PostMapping("/email")
    public boolean sendMailReject(@RequestBody EmailDTO emailDTO) {
        return emailService.sendMailReject(emailDTO);
    }
}
