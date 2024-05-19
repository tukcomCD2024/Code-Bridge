package com.Backend.shareNote.domain.Oraganization.DTOs.organdto;

import lombok.Data;

@Data
public class OrganizationInvitation {
    private String nickname;
    private String organizationId;
    private String email;
}
