package com.Backend.shareNote.domain.oauth2;

import com.Backend.shareNote.domain.Jwt.JWTUtil;
import com.Backend.shareNote.domain.User.dto.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //OAuth2User
        CustomOAuth2User customUserDetail = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetail.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt("access",username, role, 60 * 60 * 60L);

        response.addCookie(createCookie("Authorization", token));
        // 이거는 배포버전이랑 로컬이랑 다르게 해줘야 겠네
        response.sendRedirect("http://localhost:3000/main");
        // response.sendRedirect("https://sharenote.shop/main");

    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        //이 부분은 https에서만 쿠키를 전송하겠다는 의미
        //cookie.setSecure(true);
        cookie.setPath("/");
        //이 부분은 자바스크립트에서 쿠키에 접근하지 못하도록 하는 속성
        cookie.setHttpOnly(true);

        return cookie;
    }
}
