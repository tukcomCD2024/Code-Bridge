package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.dto.OrganizationCreateDTO;
import com.Backend.shareNote.domain.Oraganization.dto.OrganizationDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.dto.OrganizationUpdateDTO;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    public String createOrganization(OrganizationCreateDTO organizationCreateDTO) {
        Users user = userRepository.findByLoginId(organizationCreateDTO.getOwner())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));


        Organization org = Organization.builder()
                .name(organizationCreateDTO.getName())
                .owner(organizationCreateDTO.getOwner())
                .build();
        Organization savedOrgan = organizationRepository.save(org);

        user.getOrganizations().add(savedOrgan.getId());

        //@Transactional이 없으니까 직접 save해줘야함
        userRepository.save(user);

        return savedOrgan.getId();

    }

    public String deleteOrganization(OrganizationDeleteDTO organizationDeleteDTO) {
        Users user = userRepository.findByLoginId(organizationDeleteDTO.getUserLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Organization organization = organizationRepository.findById(organizationDeleteDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Organization 입니다."));

        user.getOrganizations().remove(organization.getId()); //좋은데?
        userRepository.save(user);
        if(organization.getOwner().equals(user.getLoginId())) {
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
