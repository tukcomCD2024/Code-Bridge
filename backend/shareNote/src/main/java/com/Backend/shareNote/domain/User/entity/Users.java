package com.Backend.shareNote.domain.User.entity;

import com.Backend.shareNote.domain.User.dto.UserSignUpDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    private List<String> organizations;

    @Field("created_at")
    private Date createdAt;



}