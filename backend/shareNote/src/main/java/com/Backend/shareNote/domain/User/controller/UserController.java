package com.Backend.shareNote.domain.User.controller;

import com.Backend.shareNote.domain.User.dto.UserLoginDTO;
import com.Backend.shareNote.domain.User.dto.UserSignUpDTO;
import com.Backend.shareNote.domain.User.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class UserController {
    private final UserService UserService;
    @PostMapping("/user/signUp")
    public ResponseEntity<?> signUp(@RequestBody UserSignUpDTO userSignUpDTO) {
        return UserService.signUp(userSignUpDTO);
    }

    @PostMapping("/user/login")
    public ResponseEntity<Object> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.error("login");
        return UserService.login(userLoginDTO);
    }

    @PostMapping("/user/uniqueEmail/{email}")
    public ResponseEntity<Boolean> emailOnly(@PathVariable String email) {
        return UserService.uniqueEmail(email);
    }

    @PostMapping("/user/uniqueNickname/{nickname}")
    public ResponseEntity<Boolean> nicknameOnly(@PathVariable String nickname) {
        return UserService.uniqueNickname(nickname);
    }

}
