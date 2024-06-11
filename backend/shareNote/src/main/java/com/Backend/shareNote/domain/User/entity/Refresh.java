package com.Backend.shareNote.domain.User.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "refresh")
@Builder
public class Refresh {
    @Id
    private String id;
    //DB 탐색을 위한
    private String userId;
    private String refresh;
    //주기적으로 스케줄 작업을 통해 만료 시간이 지난 토큰 삭제
    private String expiration;
}
