package com.Backend.shareNote.domain.Jwt;

import com.Backend.shareNote.domain.User.dto.CustomUserDetails;
import com.Backend.shareNote.domain.User.entity.Fcm;
import com.Backend.shareNote.domain.User.entity.Refresh;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.FcmRepository;
import com.Backend.shareNote.domain.User.repository.RefreshRepository;
import com.Backend.shareNote.domain.User.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    // 생성자의 매개변수가 추가되니까 SecurityConfig에도 추가해줘
    private final JWTUtil jwtUtil;
    // ObjectMapper는 JSON을 다루기 위한 클래스, request에서 JSON 추출
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RefreshRepository refreshRepository;
    private final FcmRepository fcmRepository;

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {


            // 클라이언트 요청에서 username, password 추출(이거 테스트)
            Map<String, String> requestBody = objectMapper.readValue(request.getInputStream(), Map.class);
            String email = requestBody.get("email");
            String password = requestBody.get("password");

            // 스프링 시큐리티에서 username과 password를 검증하기 위해서는 token을 생성해야함
            // 여기서 넘기는 email이 loadUserByUsername의 인자로 들어감!!!
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);

            // token 검증을 위해 AuthenticationManager로 전달
            return authenticationManager.authenticate(authToken);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    // 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication)
    throws IOException{
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        // email 반환 -> nickname으로 바꿈
        // email도 반환해 줘야 함
        String username = customUserDetails.getUsername();

        // 이 이메일로 유저 정보를 검색해서 클라이언트에 반환 필요(로컬 스토리지)
        String email = customUserDetails.getEmail();
        String userId = customUserDetails.getId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();
        //토큰 생성
        String access = jwtUtil.createJwt("access", userId, username, role, 60000000L);
        String refresh = jwtUtil.createJwt("refresh", userId, username, role, 86400000L);

        //Refresh 토큰 저장
        //이제 로그인하면 Refresh 토큰이 DB에 저장됨
        addRefreshEntity(userId, refresh, 86400000L);

        response.addHeader("access", "Bearer " + access);
        response.addHeader("refresh", "Bearer " + refresh);
        //response.addCookie(createCookie("Authorization", access));
        // 이거는 배포버전이랑 로컬이랑 다르게 해줘야 겠네
        // response.sendRedirect("http://localhost:3000");

        //fcm 저장
        String fcm = request.getHeader("fcm");
        if(fcm != null) {
            Fcm fcmToken = fcmRepository.findByUserId(userId);
            if(fcmToken != null) {
                //있으면 새로운 fcm으로 교체하기 or 똑같아도 그냥 넣어
                fcmToken.setFcm(fcm);
                fcmRepository.save(fcmToken);
            } else {
                //없으면 새로운 fcm 만들어서 저장
                Fcm newfcm = Fcm.builder()
                        .userId(userId)
                        .fcm(fcm)
                        .build();
                fcmRepository.save(newfcm);
            }

        }


        // JSON 응답 데이터 생성
        Map<String, String> responseData = new HashMap<>();
        responseData.put("name", username);
        responseData.put("userId", userId);

        // 사용자 정보를 JSON 형식으로 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseData));
    }

    private void addRefreshEntity(String userId, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        Refresh refreshToken = Refresh.builder()
                .userId(userId)
                .refresh(refresh)
                .expiration(date.toString())
                .build();


        refreshRepository.save(refreshToken);
    }



    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorData = new HashMap<>();
        if (failed instanceof BadCredentialsException) {
            errorData.put("message", "아이디 혹은 비밀번호를 확인해주세요");
        } else {
            errorData.put("message", "Authentication failed");
        }

        ResponseEntity<Map<String, String>> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorData);

        response.setStatus(responseEntity.getStatusCodeValue());
        response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
        ArrayList<Integer> ary = new ArrayList<>();

    }
}
