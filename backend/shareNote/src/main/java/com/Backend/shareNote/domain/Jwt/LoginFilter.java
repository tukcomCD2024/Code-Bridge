package com.Backend.shareNote.domain.Jwt;

import com.Backend.shareNote.domain.User.dto.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    // 생성자의 매개변수가 추가되니까 SecurityConfig에도 추가해줘
    private final JWTUtil jwtUtil;
    // ObjectMapper는 JSON을 다루기 위한 클래스, request에서 JSON 추출
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            throw new RuntimeException();
        }
    }

    // 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        // email 반환 -> nickname으로 바꿈
        String username = customUserDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 600 * 600 * 10L);

        response.addHeader("Authorization", "Bearer " + token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        // 로그인 실패시 401 응답 코드 반환
        response.setStatus(401);



    }
}
