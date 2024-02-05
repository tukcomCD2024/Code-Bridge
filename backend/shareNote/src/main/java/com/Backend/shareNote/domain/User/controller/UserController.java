package com.Backend.shareNote.domain.User.controller;

import com.Backend.shareNote.domain.User.dto.UserLoginDTO;
import com.Backend.shareNote.domain.User.dto.UserSignUpDTO;
import com.Backend.shareNote.domain.User.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService UserService;
    @PostMapping("/user/signUp")
    public String signUp(@RequestBody UserSignUpDTO userSignUpDTO) {
        UserService.signUp(userSignUpDTO);
        return "회원가입 성공";
    }

    @PostMapping("/user/login")
    public String login(@RequestBody UserLoginDTO userLoginDTO) {
        return UserService.login(userLoginDTO);
    }

}