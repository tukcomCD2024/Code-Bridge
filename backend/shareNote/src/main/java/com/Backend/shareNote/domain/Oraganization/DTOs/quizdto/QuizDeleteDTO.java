package com.Backend.shareNote.domain.Oraganization.DTOs.quizdto;

import lombok.Data;

@Data
public class QuizDeleteDTO {
    private String organizationId;
    private String noteId;
    private String userId;
    private String quizId;
}
