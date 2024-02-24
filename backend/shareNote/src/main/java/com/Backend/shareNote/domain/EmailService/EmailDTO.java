package com.Backend.shareNote.domain.EmailService;

import lombok.Data;

@Data
public class EmailDTO {
    private String nickname;
    private String targetMail;
    private String link;
}
