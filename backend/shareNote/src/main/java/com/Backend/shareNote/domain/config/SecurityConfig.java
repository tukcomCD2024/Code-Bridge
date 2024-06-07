package com.Backend.shareNote.domain.config;



import com.Backend.shareNote.domain.Jwt.JWTFilter;
import com.Backend.shareNote.domain.Jwt.JWTUtil;
import com.Backend.shareNote.domain.Jwt.LoginFilter;
import com.Backend.shareNote.domain.User.service.CustomOAuth2UserService;
import com.Backend.shareNote.domain.User.service.UserService;
import com.Backend.shareNote.domain.oauth2.CustomSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguration 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        LoginFilter loginFIlter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFIlter.setFilterProcessesUrl("/api/user/login");

        // CORS 설정
        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();
                        // singletonList는 하나의 요소만 가지는 리스트를 생성
                        configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:3000",
                                "http://shareNote.shop",
                                "https://shareNote.shop",
                                "http://192.168.45.75"
                        ));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        // 헤더에 Authorization을 추가해줘야 클라이언트에서 접근 가능
                        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                })));


        http
                .csrf((auth -> auth.disable()));
        http
                .formLogin((auth -> auth.disable()));
        http
                .httpBasic((auth -> auth.disable()));
        http
                .oauth2Login((oauth2) -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/api/oauth2/authorization"))
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );
        // 경로별 인가 작업
        http
                .authorizeHttpRequests((auth -> auth
                        .requestMatchers("/api/user/login","/","/api/user/signUp").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated())
                );
        http.addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        http
                .addFilterAt(loginFIlter, UsernamePasswordAuthenticationFilter.class);

        http
                .exceptionHandling((exception) -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint()));
        // 세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();

    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: 토큰이 없거나 만료되었습니다.");
        };
    }

}
