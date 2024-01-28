package com.Backend.shareNote.domain.Quiz.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Document(collection = "quizzes")
public class Quiz {

    @Id
    private String id;

    @Field("noteId")
    private String noteId;

    @Field("ownerId")
    private String ownerId;

    @Field("created_at")
    private Date createdAt;

    @Field("question_type")
    private String questionType;

    private String question;

    private List<String> choices; // 객관식 옵션

    private String answer;

    @Field("solverUser")
    private List<String> solverUsers;

    // 생성자, 게터, 세터 등 필요한 메서드들 추가
}