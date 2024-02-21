package com.Backend.shareNote.domain.EmailService;

import lombok.Data;

@Data
public class EmailDTO {
    private String targetMail;
    private String link;
}
