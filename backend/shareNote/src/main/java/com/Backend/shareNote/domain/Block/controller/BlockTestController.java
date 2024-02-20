package com.Backend.shareNote.domain.Block.controller;

import com.Backend.shareNote.domain.Block.dto.BlockDTO;
import com.Backend.shareNote.domain.Block.dto.ContentDTO;
import com.Backend.shareNote.domain.Block.dto.PageBlocksDTO;
import com.Backend.shareNote.domain.Block.service.BlockService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/block/content")
    public String addContent(@RequestBody ContentDTO content) {
        log.info("addContent");
        blockService.addContent(content);
        return "success";
    }

    @GetMapping("/block")
    public ResponseEntity<List<BlockDTO>> getBlockList(@RequestBody PageBlocksDTO pageBlocksDTO) {
        return blockService.getBlocks(pageBlocksDTO);

    }
}
