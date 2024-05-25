package com.Backend.shareNote.domain.Oraganization.DTOs.quizdto;

import lombok.Data;

@Data
public class QuizSearchDTO {
    private String quizId;
    private String quizTitle;
    private Integer correct;
    private String nickname;
}
