package com.Backend.shareNote.domain.Oraganization.organdto;

import lombok.Data;


@Data
public class OrganizationSearchDTO { //우선 이렇게 3개만 반환해 보자
    private String owner; // 만든 사람 아이디
    private String emoji; //이거 값이 String인가??
    private String organizationId; //조직 아이디
}
