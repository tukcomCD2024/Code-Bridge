package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.pagedto.PageCreateDTO;
import com.Backend.shareNote.domain.Oraganization.pagedto.PageDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.pagedto.PageSearchDTO;
import com.Backend.shareNote.domain.Oraganization.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PageController {
    private final PageService pageService;
    @PostMapping("/page")
    public ResponseEntity<PageSearchDTO> createPage(@RequestBody PageCreateDTO pageCreateDTO) {
        return pageService.createPage(pageCreateDTO);
    }

    @DeleteMapping("/page")
    public String deletePage(@RequestBody PageDeleteDTO pageDeleteDTO) {
        return pageService.deletePage(pageDeleteDTO);
    }


}
