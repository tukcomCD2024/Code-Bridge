package com.Backend.shareNote.domain.Oraganization.DTOs.quizdto;

import lombok.Data;

import java.util.List;

@Data
public class QuizCreateDTO {
    private String organizationId;
    private String noteId;
    private String userId;
    private String quizType;
    private String problem;
    private Integer answer;
    private List<String> problems;
    private String quizTitle;
}
