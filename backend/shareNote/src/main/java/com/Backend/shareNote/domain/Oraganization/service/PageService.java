package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.pagedto.PageCreateDTO;
import com.Backend.shareNote.domain.Oraganization.pagedto.PageDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.pagedto.PageSearchDTO;
import com.Backend.shareNote.domain.Oraganization.repository.NoteRepository;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Oraganization.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PageService {
    private final OrganizationRepository organizationRepository;
    private final PageRepository pageRepository;
    @Transactional
    public ResponseEntity<PageSearchDTO> createPage(PageCreateDTO pageCreateDTO) {
        UUID routingKey = UUID.randomUUID();
        //page 생성
        Organization.Page page = Organization.Page.builder()
                .createUser(pageCreateDTO.getCreateUserId())
                .blocks(new ArrayList<String>())
                .id(new ObjectId().toString())
                .routingKey(routingKey.toString())
                .build();

        pageRepository.save(page);

        Organization organization = organizationRepository.findById(pageCreateDTO.getOrganizationId()).get();
        organization.addPageToNote(pageCreateDTO.getNoteId(), page);
        organizationRepository.save(organization);


        PageSearchDTO pageSearchDTO = new PageSearchDTO();
        pageSearchDTO.setPageId(page.getId());
        pageSearchDTO.setRoutingKey(routingKey.toString());

        return ResponseEntity.ok(pageSearchDTO);
    }
    @Transactional
    public String deletePage(PageDeleteDTO pageDeleteDTO) {
        //page 삭제
        Organization organization = organizationRepository.findById(pageDeleteDTO.getOrganizationId()).get();
        organization.deletePageFromNote(pageDeleteDTO.getNoteId(), pageDeleteDTO.getPageId());
        organizationRepository.save(organization);
        return "success";
    }


}
