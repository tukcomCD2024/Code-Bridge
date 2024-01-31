package com.Backend.shareNote.domain.Oraganization.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Service;

import java.util.List;

@Document(collection = "organizations")
@Builder
@Getter
public class Organization {

    @Id
    private String id;

    private String name;
    @Setter
    private String description;

    private String owner; //여기 loginId 들어가는거야
    private List<String> members;

    private List<String> quiz;

    private List<Note> notes;

    // 내부 클래스로 Note 정의
    @Builder //신기하다
    @Getter
    @Setter
    public static class Note {
        @Id
        private String id;
        private String createUser;

        private String title;

        private String noteImageUrl;

        private List<Page> pages;
        //page에 order 필드 추가!!


        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }

    // 내부 클래스로 Page 정의
    public static class Page {
        @Id
        private String id;
        private String craeteUser;
        private List<String> blocks;

        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }

    // 생성자, 게터, 세터 등 필요한 메서드들 추가
}
