package com.Backend.shareNote.domain.User.service;

import com.Backend.shareNote.domain.Jwt.JwtService;
import com.Backend.shareNote.domain.Oraganization.organdto.AcceptInvitationDTO;
import com.Backend.shareNote.domain.Oraganization.service.OrganizationService;
import com.Backend.shareNote.domain.User.dto.UserLoginDTO;
import com.Backend.shareNote.domain.User.dto.UserSignUpDTO;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final OrganizationService organizationService;
    public void signUp(UserSignUpDTO userSignUpDTO) {
        Users users = Users.builder()
                .email(userSignUpDTO.getEmail())
                .password(userSignUpDTO.getPassword())
                .nickname(userSignUpDTO.getNickname())
                .organizations(new ArrayList<String>())
                .build();

        userRepository.save(users);
    }

    public ResponseEntity<Object> login(UserLoginDTO userLoginDTO) {
        try {
            Optional<Users> userOptional = userRepository.findByEmail(userLoginDTO.getEmail());

            if (userOptional.isPresent()) {
                Users loginUser = userOptional.get();
                if (!loginUser.getPassword().equals(userLoginDTO.getPassword())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호를 확인해 주세요");
                } else {
                    // 로그인 성공 시 UserId와 name을 JSON 형태로 반환
                    // 로그인 성공 시 UserId와 name을 JSON 형태로 반환
                    Map<String, Object> responseJson = new HashMap<>();
                    responseJson.put("userId", loginUser.getId());
                    responseJson.put("name", loginUser.getNickname());

                    //token 존재 시 token에 적힌 organization에 가입하는 로직 추가
                    if(userLoginDTO.getToken() != null){
                        try {
                            AcceptInvitationDTO acceptInvitationDTO = new AcceptInvitationDTO();
                            acceptInvitationDTO.setToken(userLoginDTO.getToken());
                            acceptInvitationDTO.setUserId(loginUser.getId());

                            organizationService.acceptInvitation(acceptInvitationDTO);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }



                    return ResponseEntity.ok(responseJson);
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디를 확인해 주세요");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다");
        }
    }
}
