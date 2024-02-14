package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.organdto.OrganizationCreateDTO;
import com.Backend.shareNote.domain.Oraganization.organdto.OrganizationDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.organdto.OrganizationUpdateDTO;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Quiz.entity.Quiz;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    public ResponseEntity<Object> createOrganization(OrganizationCreateDTO organizationCreateDTO) {
        Users user = userRepository.findByEmail(organizationCreateDTO.getOwner())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));


        Organization org = Organization.builder()
                .name(organizationCreateDTO.getName())
                .owner(organizationCreateDTO.getOwner())
                .notes(new ArrayList<Organization.Note>())
                .quiz(new ArrayList<String>())
                .members(new ArrayList<String>())
                .description("")
                .build();
        Organization savedOrgan = organizationRepository.save(org);

        user.getOrganizations().add(savedOrgan.getId());

        //@Transactional이 없으니까 직접 save해줘야함
        userRepository.save(user);
        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put("organizationId", savedOrgan.getId());
        return ResponseEntity.ok(responseJson);

    }

    public String deleteOrganization(OrganizationDeleteDTO organizationDeleteDTO) {
        Users user = userRepository.findByEmail(organizationDeleteDTO.getUserLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Organization organization = organizationRepository.findById(organizationDeleteDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Organization 입니다."));

        user.getOrganizations().remove(organization.getId()); //좋은데?
        userRepository.save(user);
        if(organization.getOwner().equals(user.getEmail())) {
            organizationRepository.delete(organization);
            return "삭제 완료";
        }
        else {
            //throw new IllegalArgumentException("권한이 없습니다.");
            return "권한이 없습니다.";
        }
    }

    public String updateOrganization(OrganizationUpdateDTO organizationUpdateDTO) {
        Organization organization = organizationRepository.findById(organizationUpdateDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Organization 입니다."));

        organization.setDescription(organizationUpdateDTO.getDescription());
        organizationRepository.save(organization);

        return "수정 완료";

    }


}
