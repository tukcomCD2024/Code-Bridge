package com.Backend.shareNote.domain.Block.service;

import com.Backend.shareNote.domain.Block.dto.BlockDTO;
import com.Backend.shareNote.domain.Block.dto.ContentDTO;
import com.Backend.shareNote.domain.Block.dto.PageBlocksDTO;
import com.Backend.shareNote.domain.Block.entity.Block;
import com.Backend.shareNote.domain.Block.repository.BlockRepository;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Oraganization.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockService {
    private final BlockRepository blockRepository;
    private final PageRepository pageRepository;
    private final OrganizationRepository organizationRepository;
    // 블록에 저장하는 로직
    @Transactional
    public void createBlock(ContentDTO content) {
        log.info("값 없냐?" + content.getCreateUser());
        //block 생성 로직 builder로 만들자
        Block block = Block.builder()
                .type(content.getType())
                .blockSeq(content.getBlockSeq())
                .createUser(content.getCreateUser())
                .pageId(content.getPageId())
                .build();

        blockRepository.save(block); // 이때 Id가 할당되는 듯

        //organization에 블록 추가 메서드
        Organization organization = organizationRepository.findById(content.getOrganizationId()).get();
        organization.addBlockToPage(
                content.getNoteId(),
                content.getPageId(),
                block.getId());

        organizationRepository.save(organization);


    }

    @Transactional
    public void addContent(ContentDTO contentDTO) {
        //block에 content 추가 로직
        Block block = blockRepository.findById(contentDTO.getBlockId()).get();

        Block.Content content = Block.Content.builder()
                .type(contentDTO.getType())
                .content(contentDTO.getContent())
                .blockId(contentDTO.getBlockId())
                .blockSeq(contentDTO.getBlockSeq())
                .nickName(contentDTO.getNickName())
                .crdtIndex(contentDTO.getCrdtIndex())
                .createUser(contentDTO.getCreateUser())
                .routingKey(contentDTO.getRoutingKey())
                .build();

        block.addContent(content);

        blockRepository.save(block);
    }

    // 페이지에 있는 블록들 조회하는 로직
    public ResponseEntity<List<BlockDTO>> getBlocks(PageBlocksDTO pageData) {
        List<Block> blocks = new ArrayList<>();
        Organization org = organizationRepository.findById(pageData.getOrganizationId()).get();
        org.getNotes().forEach(note -> {
            if (note.getId().equals(pageData.getNoteId())) {
                note.getPages().forEach(page -> {
                    if (page.getId().equals(pageData.getPageId())) {
                        page.getBlocks().forEach(blockId -> {
                            blocks.add(blockRepository.findById(blockId).get());
                        });
                        log.info("블록 조회 성공");
                    }
                });
            }
        });

        List<BlockDTO> blockDTOs = convertToDTO(blocks);
        return ResponseEntity.ok().body(blockDTOs);
    }

    public List<BlockDTO> convertToDTO(List<Block> blocks) {
        List<BlockDTO> blockDTOs = new ArrayList<>();
        for (Block block : blocks) {
            BlockDTO blockDTO = new BlockDTO();
            blockDTO.setId(block.getId());
            blockDTO.setType(block.getType());
            blockDTO.setBlockSeq(block.getBlockSeq());
            blockDTO.setCreateUser(block.getCreateUser());

            List<BlockDTO.ContentDTO> contentDTOs = block.getContent().stream()
                    .map(content -> {
                        BlockDTO.ContentDTO contentDTO = new BlockDTO.ContentDTO();
                        contentDTO.setType(content.getType());
                        contentDTO.setContent(content.getContent());
                        contentDTO.setBlockId(content.getBlockId());
                        contentDTO.setBlockSeq(content.getBlockSeq());
                        contentDTO.setNickName(content.getNickName());
                        contentDTO.setCrdtIndex(content.getCrdtIndex());
                        return contentDTO;
                    })
                    .collect(Collectors.toList());

            blockDTO.setContent(contentDTOs);
            blockDTOs.add(blockDTO);
        }
        return blockDTOs;
    }

}
