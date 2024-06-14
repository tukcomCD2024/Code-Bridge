package com.Backend.shareNote.domain.User.service;

import com.Backend.shareNote.domain.User.dto.*;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 값이 궁금하니까 찍어봄
        System.out.println(oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if(registrationId.equals("naver")){
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if(registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }

        String socialId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Users existData = userRepository.findBySocialId(socialId);
        if(existData == null) {
            Users user = Users.builder()
                    .socialId(socialId)
                    .email(oAuth2Response.getEmail())
                    .nickname(oAuth2Response.getName())
                    .organizations(new ArrayList<String>())
                    .role("ROLE_USER").build();
            userRepository.save(user);

            UserDTO userDTO = new UserDTO();

            // uuid로 교체
            userDTO.setId(user.getId());
            userDTO.setSocialId(user.getSocialId());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setEmail(oAuth2Response.getEmail());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);

        }else {
            existData.updateSocialInfo(oAuth2Response.getEmail(), oAuth2Response.getName());
            userRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setId(existData.getId());
            userDTO.setSocialId(existData.getSocialId());
            userDTO.setEmail(existData.getEmail());
            userDTO.setName(existData.getNickname());
            userDTO.setRole(existData.getRole());

            return new CustomOAuth2User(userDTO);

        }


    }

}
