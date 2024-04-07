package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.DTOs.pagedto.PageCreateDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.pagedto.PageDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.pagedto.PageSearchDTO;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Oraganization.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PageService {
    private final OrganizationRepository organizationRepository;
    private final PageRepository pageRepository;

    @Transactional
    public ResponseEntity<PageSearchDTO> createPage(PageCreateDTO pageCreateDTO) {

        //page 생성
        Organization.Page page = Organization.Page.builder()
                .createUser(pageCreateDTO.getCreateUserId())
//               .id(new ObjectId().toString())
                .build();

        pageRepository.save(page);

        Organization organization = organizationRepository.findById(pageCreateDTO.getOrganizationId()).get();
        organization.addPageToNote(pageCreateDTO.getNoteId(), page);
        organizationRepository.save(organization);


        PageSearchDTO pageSearchDTO = new PageSearchDTO();
        pageSearchDTO.setPageId(page.getId());


        return ResponseEntity.ok(pageSearchDTO);
    }
    @Transactional
    public boolean deletePage(PageDeleteDTO pageDeleteDTO) {
        //page 삭제
        try{
            Organization organization = organizationRepository.findById(pageDeleteDTO.getOrganizationId()).get();
            organization.deletePageFromNote(pageDeleteDTO.getNoteId(), pageDeleteDTO.getPageId());
            organizationRepository.save(organization);
            return true;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }

    }


    public ResponseEntity<List<PageSearchDTO>> getPages(PageCreateDTO pageCreateDTO) {
        //note에 있는 page들 가져오기
        Organization organization = organizationRepository.findById(pageCreateDTO.getOrganizationId()).get();
        Organization.Note note = organization.getNotes().stream()
                .filter(n -> n.getId().equals(pageCreateDTO.getNoteId()))
                .findFirst()
                .get();

        List<PageSearchDTO> pageSearchDTOList = new ArrayList<>();
        for(Organization.Page page : note.getPages()){
            PageSearchDTO pageSearchDTO = new PageSearchDTO();
            pageSearchDTO.setPageId(page.getId());
            pageSearchDTOList.add(pageSearchDTO);
        }

        return ResponseEntity.ok(pageSearchDTOList);

    }
}
