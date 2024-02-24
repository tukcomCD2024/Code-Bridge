package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.organdto.*;
import com.Backend.shareNote.domain.Oraganization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrganizationController {
    private final OrganizationService organizationService;
    @PostMapping("/user/organization")
    public ResponseEntity<OrganizationSearchDTO> createOrganization(@RequestBody OrganizationCreateDTO organizationCreateDTO) {
        return organizationService.createOrganization(organizationCreateDTO);

    }

    @DeleteMapping("/user/organization")
    public ResponseEntity<Object> deleteOrganization(@RequestBody OrganizationDeleteDTO organizationCreateDTO) {
        return organizationService.deleteOrganization(organizationCreateDTO);

    }

    @PatchMapping("/user/organization")
    public String updateOrganization(@RequestBody OrganizationUpdateDTO organizationUpdateDTO) {
        return organizationService.updateOrganization(organizationUpdateDTO);

    }

    //@GetMapping("/user/organization")

    //초대 링크를 생성하고 이메일로 보내기
    @PostMapping("/user/organization/invitation")
    public ResponseEntity<Object> inviteOrganization(@RequestBody OrganizationInvitation Invitation) {
        return organizationService.inviteOrganization(Invitation);
    }

    //링크 눌렀을 때 로그인 상태면 초대에 응하겠습니까?? 예 클릭 시 이 url 동작
    @PostMapping("/user/organization/invitation/accept")
    public ResponseEntity<Object> acceptOrganization(@RequestBody AcceptInvitationDTO invitation) {
        return organizationService.acceptInvitation(invitation);
    }


}
