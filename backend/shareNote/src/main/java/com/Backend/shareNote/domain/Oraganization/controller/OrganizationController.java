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
    public String inviteOrganization(@RequestBody OrganizationInvitation Invitation) {
        return organizationService.inviteOrganization(Invitation);
    }

    //login 성공 시 token이 존재한다면 실행됨
    @PostMapping("/user/organization/invitation/accept")
    public ResponseEntity<Object> acceptOrganization(@RequestBody AcceptInvitationDTO invitation) {
        return organizationService.acceptInvitation(invitation);
    }


}
