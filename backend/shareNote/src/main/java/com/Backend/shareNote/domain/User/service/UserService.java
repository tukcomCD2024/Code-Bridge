package com.Backend.shareNote.domain.User.service;

import com.Backend.shareNote.domain.User.dto.UserLoginDTO;
import com.Backend.shareNote.domain.User.dto.UserSignUpDTO;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    public void signUp(UserSignUpDTO userSignUpDTO) {
        Users users = Users.builder()
                .email(userSignUpDTO.getEmail())
                .password(userSignUpDTO.getPassword())
                .nickname(userSignUpDTO.getNickname())
                .organizations(new ArrayList<String>())
                .build();

        userRepository.save(users);
    }

    public String login(UserLoginDTO userLoginDTO) {

        try{
            Optional<Users> user = userRepository.findByEmail(userLoginDTO.getEmail()); //여기서 에러나면 에러 메세지 지정이 힘드니까 try catch로 해결
            Users loginUser = user.get();
            if(!loginUser.getPassword().equals(userLoginDTO.getPassword())) {
                return "비밀번호를 확인해 주세요";
            }
            return loginUser.getId();
        }catch (Exception e){
            return "아이디를 확인해 주세요";
        }




    }
}
