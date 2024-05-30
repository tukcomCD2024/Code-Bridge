package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.service.ContributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class ContributeController {
    private final ContributionService contributionService;
    @GetMapping("/contribute/{organizationId}")
    public ResponseEntity<?> getContribution(@PathVariable String organizationId){
        return contributionService.getContribution(organizationId);

    }
}
