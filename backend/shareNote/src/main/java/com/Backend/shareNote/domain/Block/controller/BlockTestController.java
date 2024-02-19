package com.Backend.shareNote.domain.Block.controller;

import com.Backend.shareNote.domain.Block.dto.ContentDTO;
import com.Backend.shareNote.domain.Block.service.BlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class BlockTestController {
    private final BlockService blockService;
    //DB 변경이 어떤 식으로 이루어지는지 확인하기 위한 테스트용 컨트롤러
    @PostMapping("/block")
    public String createBlock(@RequestBody ContentDTO content) {
        log.info("createBlock");
        blockService.createBlock(content);
        return "success";
    }
}
