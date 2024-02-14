package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.organdto.OrganizationCreateDTO;
import com.Backend.shareNote.domain.Oraganization.organdto.OrganizationDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.organdto.OrganizationUpdateDTO;
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
    public ResponseEntity<Object> createOrganization(@RequestBody OrganizationCreateDTO organizationCreateDTO) {
        return organizationService.createOrganization(organizationCreateDTO);

    }

    @DeleteMapping("/user/organization")
    public String deleteOrganization(@RequestBody OrganizationDeleteDTO organizationCreateDTO) {
        return organizationService.deleteOrganization(organizationCreateDTO);

    }

    @PatchMapping("/user/organization")
    public String updateOrganization(@RequestBody OrganizationUpdateDTO organizationUpdateDTO) {
        return organizationService.updateOrganization(organizationUpdateDTO);

    }


}
