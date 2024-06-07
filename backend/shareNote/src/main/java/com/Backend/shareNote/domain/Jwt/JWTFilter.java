package com.Backend.shareNote.domain.Jwt;

import com.Backend.shareNote.domain.User.dto.CustomUserDetails;
import com.Backend.shareNote.domain.User.entity.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.valves.rewrite.InternalRewriteMap;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final List<String> permitAllUrls = List.of("/api/user/login", "/api/user/signUp", "/");
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // permitAll 경로에 대해서는 필터를 건너뛰도록 설정
        // JWT 인증이 필요없는 permitAll 한 url들에 대해서는 건너뛰자
        if (permitAllUrls.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // request에서 Authorization 헤더를 찾음
        //String authorization = request.getHeader("Authorization");
        String authorization = null;
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            log.error("cookie가 없음");
            filterChain.doFilter(request, response);
            return;
        }
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("Authorization")) {
                authorization = cookie.getValue();
            }
        }



        // 쿠키에서 추출한거
        String token = authorization;

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token) || token == null) {
            log.error("토큰이 만료됨거나 없음");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token is expired");
            //filterChain.doFilter(request, response);
            // 조건이 만료되면 메서드 종료 (필수)
            return;
        }

        // 토큰에서 username과 role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // userEntity를 생성하여 값 설정
        Users userEntity = Users.builder()
                .nickname(username)
                // 임시로 만들라는데?
                .password("tempPassword")
                .role(role)
                .build();

        // UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        // 스프링 시큐리티 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 세선에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

    }


}
