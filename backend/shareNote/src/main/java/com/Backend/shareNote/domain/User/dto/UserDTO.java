package com.Backend.shareNote.domain.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String id;
    private String role;
    private String name;
    private String socialId;
    private String email;
}
