package com.Backend.shareNote.domain.Block.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
@Getter
@Builder
@Document(collection = "blocks")
public class Block {

    @Id
    private String id;

    private String type;

    private List<Content> content;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    @Field("좋아요")
    private Like like;

    // 내부 클래스로 Content 정의
    public static class Content {
        private String oneChar;
        private String id;
        private List<Integer> position;
        private String userId;

        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }

    // 내부 클래스로 Like 정의
    public static class Like {
        @Field("noteId")
        private String noteId;

        @Field("contributor")
        private List<String> contributors;

        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }

    // 생성자, 게터, 세터 등 필요한 메서드들 추가
}