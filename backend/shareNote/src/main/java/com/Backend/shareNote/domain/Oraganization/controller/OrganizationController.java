package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.dto.OrganizationCreateDTO;
import com.Backend.shareNote.domain.Oraganization.dto.OrganizationDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.dto.OrganizationUpdateDTO;
import com.Backend.shareNote.domain.Oraganization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    @PostMapping("/user/organization")
    public String createOrganization(@RequestBody OrganizationCreateDTO organizationCreateDTO) {
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
