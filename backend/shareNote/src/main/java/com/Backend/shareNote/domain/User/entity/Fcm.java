package com.Backend.shareNote.domain.User.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "fcm")
@Builder
public class Fcm {
    @Id
    private String id;
    private String userId;
    @Setter
    private String fcm;
    private String expiration;
}
