package com.Backend.shareNote.domain.Block.service;

import com.Backend.shareNote.domain.Block.dto.ContentDTO;
import com.Backend.shareNote.domain.Block.entity.Block;
import com.Backend.shareNote.domain.Block.repository.BlockRepository;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Oraganization.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void addContent(ContentDTO content) {
        //block에 content 추가 로직

    }

}
