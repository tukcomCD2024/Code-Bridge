package com.Backend.shareNote.domain.Oraganization.DTOs.contributiondto;

import lombok.Data;

@Data
public class ContributionResultDTO {
    private String userId;
    private String nickname;
    private Integer quizScore;
    private Integer likeScore;
}
