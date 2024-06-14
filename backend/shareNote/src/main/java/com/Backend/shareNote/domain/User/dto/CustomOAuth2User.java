package com.Backend.shareNote.domain.User.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final UserDTO userDTO;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getRole();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return userDTO.getName();
    }

    public String getSocialId() {
        return userDTO.getSocialId();
    }

    public String getEmail() {
        return userDTO.getEmail();
    }

    public String getId() {
        return userDTO.getId();
    }

    // 내 생각에 여기에 nickname 반환하는 메서드도 넣어야해
}
