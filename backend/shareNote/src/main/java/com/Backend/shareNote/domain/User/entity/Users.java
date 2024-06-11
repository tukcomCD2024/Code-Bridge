package com.Backend.shareNote.domain.User.entity;

import com.Backend.shareNote.domain.User.dto.UserSignUpDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Getter
@Document(collection = "users")
@Builder
public class Users {

    @Id
    private String id;

    private String email;

    private String password;

    private String nickname;

    // 소셜 로그인 시 가입 여부 확인을 위한 속성
    private String socialId;

    @Setter
    private List<String> organizations;

    @CreatedDate
    private LocalDateTime createdAt;

    private String role;

    public void updateSocialInfo(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }



}