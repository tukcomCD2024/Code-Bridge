package com.Backend.shareNote.domain.Oraganization.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
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

    private String owner; //여기 email 들어가는거야
    private List<String> members;

    private List<String> quiz;

    private List<Note> notes;

    private String emoji;

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
    @Getter
    @Builder
    public static class Page {
        @Id //수동으로 id 생성
        private String id;
        private String createUser;
        private List<String> blocks;
        private String routingKey;

        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }

    public void addPageToNote(String noteId, Page newPage) {
        for (Note note : this.notes) {
            if (note.getId().equals(noteId)) {
                note.getPages().add(newPage);
                break;
            }
        }
    }

    public void deletePageFromNote(String noteId, String pageId) {
        // 페이지 삭제
        for (Note note : this.notes) {
            if (note.getId().equals(noteId)) {
                note.getPages().removeIf(page -> page.getId().equals(pageId));
                break;
            }
        }
    }

    // 생성자, 게터, 세터 등 필요한 메서드들 추가
}
