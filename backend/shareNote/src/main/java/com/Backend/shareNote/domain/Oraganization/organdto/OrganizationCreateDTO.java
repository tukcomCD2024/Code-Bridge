package com.Backend.shareNote.domain.Oraganization.organdto;

import lombok.Data;

@Data
public class OrganizationCreateDTO {
    private String name;
    private String owner;
    private String emoji; //이거 값이 String인가??
    private String userId;
}
