package com.Backend.shareNote.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://sharenote.shop", "https://sharenote.shop", "http://localhost:3000", "http://192.168.45.75")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("Authorization", "Content-Type", "access", "refresh", "fcm", "SocialAccess")
                .exposedHeaders("Custom-Header", "Set-Cookie", "Authorization", "access", "refresh", "fcm", "SocialAccess")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
