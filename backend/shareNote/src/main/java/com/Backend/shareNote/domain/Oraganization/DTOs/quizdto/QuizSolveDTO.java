package com.Backend.shareNote.domain.Oraganization.DTOs.quizdto;

import lombok.Data;

@Data
public class QuizSolveDTO {
    private String organizationId;
    private String noteId;
    private String userId;
    private String quizId;
    private Integer answer;
    private String nickname;
}
