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
import java.util.Collections;
import java.util.Comparator;
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

        //Content 만들기
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
        //Content block에 장착
        block.addContent(content);

        //block 저장
        blockRepository.save(block);
    }

    // 페이지에 있는 블록들 조회하는 로직
    public ResponseEntity<List<BlockDTO>> getBlocks(PageBlocksDTO pageData) { //org, page, note 정보 포함
        //block 조회 로직
        List<Block> blocks = new ArrayList<>();
        Organization org = organizationRepository.findById(pageData.getOrganizationId()).get();
        org.getNotes().forEach(note -> {
            if (note.getId().equals(pageData.getNoteId())) { //노트 발견
                note.getPages().forEach(page -> {
                    if (page.getId().equals(pageData.getPageId())) { // 페이지 발견 -> 블록 ObjectId로 조회 -> list에 때려박아
                        page.getBlocks().forEach(blockId -> {
                            blocks.add(blockRepository.findById(blockId).get());
                        });
                        log.info("블록 조회 성공");
                    }
                });
            }
        });
        //왜인지는 모르겠지만 DTO로 변환하는게 필요하다. 그냥 반환하려니까 오류 엄청 나던데??
        //Serialization이 안된다고 에러 나왔어 아마 block 안에 있는 content가 문제였나봐
        List<BlockDTO> blockDTOs = convertToDTO(blocks);
        return ResponseEntity.ok().body(blockDTOs);
    }

    //blockDTOs를 정렬을 해서 반환해줘야 해!!
    public List<BlockDTO> convertToDTO(List<Block> blocks) {
        List<BlockDTO> blockDTOs = blocks.stream().map(block -> {
                    BlockDTO blockDTO = new BlockDTO();
                    blockDTO.setId(block.getId());
                    blockDTO.setType(block.getType());
                    blockDTO.setBlockSeq(block.getBlockSeq());
                    blockDTO.setCreateUser(block.getCreateUser());

                    // Content를 DTO로 변환
                    // 졸라 긴 삼항 연산자
                    List<BlockDTO.ContentDTO> contentDTOs = block.getContent() != null ? block.getContent().stream()
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
                            .collect(Collectors.toList()) : Collections.emptyList();

                    blockDTO.setContent(contentDTOs);
                    return blockDTO;
                })
                .sorted(Comparator.comparingInt(BlockDTO::getBlockSeq)) // BlockSeq를 기준으로 정렬
                .collect(Collectors.toList());

        return blockDTOs;
    }


}
