package com.Backend.shareNote.domain.Oraganization.DTOs.organdto;

import lombok.Data;

@Data
public class AcceptInvitationDTO {
    private String token;
    private String userId; //uid 겟죠?
}
