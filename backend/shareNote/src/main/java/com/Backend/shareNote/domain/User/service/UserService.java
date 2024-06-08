package com.Backend.shareNote.domain.User.service;

import com.Backend.shareNote.domain.Jwt.JWTUtil;
import com.Backend.shareNote.domain.Oraganization.DTOs.organdto.AcceptInvitationDTO;
import com.Backend.shareNote.domain.Oraganization.service.OrganizationService;
import com.Backend.shareNote.domain.User.dto.UserLoginDTO;
import com.Backend.shareNote.domain.User.dto.UserSignUpDTO;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final BCryptPasswordEncoder bCryptEncoder;

    private final JWTUtil jwtUtil;

    public ResponseEntity<?> signUp(UserSignUpDTO userSignUpDTO) {
        try {
            boolean isExist = userRepository.existsByEmail(userSignUpDTO.getEmail());

            if (isExist) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }

            Users users = Users.builder()
                    .email(userSignUpDTO.getEmail())
                    // 암호화 해서 비밀번호 저장
                    .password(bCryptEncoder.encode(userSignUpDTO.getPassword()))
                    .nickname(userSignUpDTO.getNickname())
                    .organizations(new ArrayList<String>())
                    .role("ROLE_USER")
                    .build();

            userRepository.save(users);

            return ResponseEntity.ok("회원가입 성공");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

//    public ResponseEntity<Object> login(UserLoginDTO userLoginDTO) {
//        try {
//            Optional<Users> userOptional = userRepository.findByEmail(userLoginDTO.getEmail());
//
//            if (userOptional.isPresent()) {
//                Users loginUser = userOptional.get();
//                if (!loginUser.getPassword().equals(userLoginDTO.getPassword())) {
//                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호를 확인해 주세요");
//                } else {
//                    // 로그인 성공 시 UserId와 name을 JSON 형태로 반환
//                    // 로그인 성공 시 UserId와 name을 JSON 형태로 반환
//                    Map<String, Object> responseJson = new HashMap<>();
//                    responseJson.put("userId", loginUser.getId());
//                    responseJson.put("name", loginUser.getNickname());
//
//                    //token 존재 시 token에 적힌 organization에 가입하는 로직 추가
//                    if(userLoginDTO.getToken() != null){
//                        try {
//                            AcceptInvitationDTO acceptInvitationDTO = new AcceptInvitationDTO();
//                            acceptInvitationDTO.setToken(userLoginDTO.getToken());
//                            acceptInvitationDTO.setUserId(loginUser.getId());
//
//                            organizationService.acceptInvitation(acceptInvitationDTO);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//
//
//
//                    return ResponseEntity.ok(responseJson);
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디를 확인해 주세요");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다");
//        }
//    }

    public ResponseEntity<Boolean> uniqueEmail(String email) {
        Optional<Users> userOptional = userRepository.findByEmail(email);
        return ResponseEntity.ok(userOptional.isEmpty());
    }

    public ResponseEntity<Boolean> uniqueNickname(String nickname) {
        Optional<Users> userOptional = userRepository.findByNickname(nickname);
        return ResponseEntity.ok(userOptional.isEmpty());
    }


    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = request.getHeader("refresh");
        //Refresh 토큰이 존재하는지 확인
        if(refresh == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("refresh 토큰이 없습니다.");
        }

        //Bearer랑 토큰 분리
        refresh = refresh.split(" ")[1];

        //Refresh 토큰이 만료되었는지 확인
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("refresh 토큰이 만료되었습니다.");
        }

        //토큰이 refresh인지 확인
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals("refresh")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("refresh 토큰이 아닙니다.");
        }
        //다시 만들 재료 뽑기
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //새로운 토큰 발급
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);
        //헤더에 넣기
        response.setHeader("access", "Bearer " + newAccess);
        response.setHeader("refresh", "Bearer " + newRefresh);

        return new ResponseEntity<>(HttpStatus.OK);


    }
}
