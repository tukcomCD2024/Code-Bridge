package com.Backend.shareNote.domain.Oraganization.DTOs.quizdto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizDetailResDTO {
    private String quizTitle;
    private String quizType;
    private String noteName; // 노트 이름
    private List<String> problems;
    private Integer correct;
}
