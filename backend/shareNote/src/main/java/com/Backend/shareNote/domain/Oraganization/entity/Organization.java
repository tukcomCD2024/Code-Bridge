package com.Backend.shareNote.domain.Oraganization.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "organizations")
@Builder
public class Organization {

    @Id
    private String id;

    private String name;

    private String description;

    private String owner;

    private List<String> members;

    private List<String> quiz;

    private List<Note> notes;

    // 내부 클래스로 Note 정의
    public static class Note {
        @Id
        private String id;

        private String title;

        private List<Page> pages; //도큐먼트 안의 도큐먼트는 어떻게 표현하지??
        //page에 order 필드 추가!!


        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }

    // 내부 클래스로 Page 정의
    public static class Page {
        @Id
        private String id;

        private String title;

        private List<String> blocks;

        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }

    // 생성자, 게터, 세터 등 필요한 메서드들 추가
}
