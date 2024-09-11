package com.Backend.shareNote.domain.Jwt;

import com.Backend.shareNote.domain.User.dto.CustomUserDetails;
import com.Backend.shareNote.domain.User.entity.Users;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    private final List<String> permitAllUrls = List.of("/api/user/login", "/api/user/signUp", "/", "/api/user/reissue",
            "/api/user/cookieToJwt","/api/user/uniqueEmail/.*","/api/user/uniqueNickname/.*","/swagger-ui.html"
            ,"/api/user/organization/invitation/accept", "/api/image", "/api/organization/invitation/approve");
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // permitAll 경로에 대해서는 필터를 건너뛰도록 설정
        // JWT 인증이 필요없는 permitAll 한 url들에 대해서는 건너뛰자
        if (permitAllUrls.stream().anyMatch(urlPattern -> Pattern.matches(urlPattern, requestURI))) {
            filterChain.doFilter(request, response);
            return;
        }

        // request에서 Authorization 헤더를 찾음
        String access = request.getHeader("access");



        if(access == null){
            log.error("토큰이 없음 url: " + requestURI);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT 토큰이 없습니다.");
            //filterChain.doFilter(request, response);
            // 조건이 만료되면 메서드 종료 (필수)
            return;
        }

        //Bearer 토큰 분리
        String token = access.split(" ")[1];

        //토큰 만료 여부 검증
        try {
            jwtUtil.isExpired(token);
        }catch (ExpiredJwtException e) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token 만료됨");

            //response status code
            //프론트와 상의해서 상태코드를 결정한 후 재발급 로직 구현
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(token);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token(refresh token을 사용했을지도?)");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
