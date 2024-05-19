package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.DTOs.pagedto.PageCreateDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.pagedto.PageDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.pagedto.PageSearchDTO;
import com.Backend.shareNote.domain.Oraganization.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Object> deletePage(@RequestBody PageDeleteDTO pageDeleteDTO) {
        boolean isDeleted = pageService.deletePage(pageDeleteDTO);

        if (isDeleted) {
            return ResponseEntity.ok().build();
        } else {
            // 적절한 에러 메시지와 함께 404 Not Found 또는 다른 상태 코드 반환
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/page/search")
    public ResponseEntity<List<PageSearchDTO>> getPage(@RequestBody PageCreateDTO pageCreateDTO) {
        return pageService.getPages(pageCreateDTO);
    }

}
