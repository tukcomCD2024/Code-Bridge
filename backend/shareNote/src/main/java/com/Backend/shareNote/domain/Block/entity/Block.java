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

    private int blockSeq; // block 순서

    private String createUser;
    private String pageId;

    @Field("created_at") // 애너테이션 붙여서 처리 좀 해줘 2/19
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    @Field("좋아요")
    private Like like;

    // 내부 클래스로 Content 정의
    @Getter
    @Builder
    public static class Content {
        private String type;
        private String content;
        private String blockId; // block 고유값
        private int blockSeq; // block 순서
        private String createUser;
        private String nickName;
        private String crdtIndex; // 타입은 뭔지 모르겠네 아마 float?
        private String routingKey;
        // 생성자, 게터, 세터 등 필요한 메서드들 추가
    }
    public void addContent(Content content) {
        this.content.add(content);
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