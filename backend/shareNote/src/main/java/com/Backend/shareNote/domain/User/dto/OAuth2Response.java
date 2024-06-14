package com.Backend.shareNote.domain.User.dto;

public interface OAuth2Response {
    // 제공자 (naver, google, kakao)
    String getProvider();
    // 제공자에게 발급해주는 아이디(번호)
    String getProviderId();
    // 이메일
    String getEmail();
    // 사용자 실명 (설정한 이름)
    String getName();

    // 여기에 닉네임을 넣어야 할거 같아
    // Name이 어떤 형태인지 보고나서 결정하자
}
