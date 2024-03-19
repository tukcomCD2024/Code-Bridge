package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.EmailService.EmailDTO;
import com.Backend.shareNote.domain.EmailService.EmailService;
import com.Backend.shareNote.domain.Jwt.JwtService;
import com.Backend.shareNote.domain.Oraganization.organdto.*;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Quiz.entity.Quiz;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    public ResponseEntity<OrganizationSearchDTO> createOrganization(OrganizationCreateDTO organizationCreateDTO) {
        Users user = userRepository.findByEmail(organizationCreateDTO.getOwner())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        //organization 생성자를 member 필드에 넣기
        ArrayList<String> members = new ArrayList<>();
        members.add(user.getId());

        Organization org = Organization.builder()
                .name(organizationCreateDTO.getName())
                .owner(organizationCreateDTO.getOwner())
                .emoji(organizationCreateDTO.getEmoji()) // 이모지 추가
                .notes(new ArrayList<Organization.Note>())
                .quiz(new ArrayList<String>())
                .members(members)
                .description("")
                .build();
        Organization savedOrgan = organizationRepository.save(org);

        user.getOrganizations().add(savedOrgan.getId());

        //@Transactional이 없으니까 직접 save해줘야함
        userRepository.save(user);

        //반환용 DTO로 반환하자
        OrganizationSearchDTO organizationSearchDTO = new OrganizationSearchDTO();
        organizationSearchDTO.setOwner(savedOrgan.getOwner());
        organizationSearchDTO.setEmoji(savedOrgan.getEmoji());
        organizationSearchDTO.setOrganizationId(savedOrgan.getId());
        return ResponseEntity.ok(organizationSearchDTO);


    }

    public ResponseEntity<Object> deleteOrganization(OrganizationDeleteDTO organizationDeleteDTO) {
        Users user = userRepository.findByEmail(organizationDeleteDTO.getUserLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Organization organization = organizationRepository.findById(organizationDeleteDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Organization 입니다."));

        user.getOrganizations().remove(organization.getId()); //좋은데?
        userRepository.save(user);

        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put("organizationId", organization.getId());
        if(organization.getOwner().equals(user.getEmail())) {
            organizationRepository.delete(organization);
            return ResponseEntity.ok(responseJson);
        }
        else {
            //throw new IllegalArgumentException("권한이 없습니다.");
            return ResponseEntity.status(401).body("권한이 없습니다.");
        }
    }

    //update는 진짜 나중에 하자 2/18
    public String updateOrganization(OrganizationUpdateDTO organizationUpdateDTO) {
        Organization organization = organizationRepository.findById(organizationUpdateDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Organization 입니다."));

        organization.setDescription(organizationUpdateDTO.getDescription());
        organizationRepository.save(organization);

        return "수정 완료";

    }


    public ResponseEntity<Object> inviteOrganization(OrganizationInvitation invitation) {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTargetMail(invitation.getEmail());
        emailDTO.setNickname(invitation.getNickname());
        //organization을 재료로 토큰 만들기
        String Token = jwtService.createInvitationToken(invitation.getOrganizationId());
        log.info("토큰 생성 완료 : " + Token);
        //쿼리 파라미터로 동작
        //이 링크 클릭 시 토큰을 localStorage에 저장하고
        //login 시 토큰을 가져와서 organization 초대 수락하기
        //emailDTO.setLink("http://localhost:3000/organization/invitation/approve?token=" + Token);
        emailDTO.setLink("http://sharenote.shop/organization/invitation/approve?token=" + Token);
        emailService.sendMail(emailDTO);
        return ResponseEntity.ok("초대장 전송 완료");
    }

    //초대 응답 로직
    //토큰에서 organizationId를 가져오기
    public ResponseEntity<Object> acceptInvitation(AcceptInvitationDTO invitation) {
        String token = invitation.getToken();
        String OrganizationId = jwtService.extractOrganizationId(token).get();
        try {
            //초대장에 있는 organization 찾아서
            organizationRepository.findById(OrganizationId).ifPresent(organization -> {
                //멤버 이미 존재하는지 확인
                organization.getMembers().forEach(member -> {
                    if (member.equals(invitation.getUserId())) {
                        throw new IllegalArgumentException("이미 초대된 사용자입니다.");
                    }
                });
                //멤버 추가
                organization.getMembers().add(invitation.getUserId());

                organizationRepository.save(organization);
            });
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(401).body("초대 수락 실패");
        }

        //user에도 organization 추가
        Users user = userRepository.findById(invitation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        user.getOrganizations().add(OrganizationId);
        userRepository.save(user);

        return ResponseEntity.ok("초대 수락 완료");
    }

    //organization 조회
    public ResponseEntity<List<OrgSearchDTO>> getOrganization(String userId) {
        // 유저 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        // 유저의 organization 조회
        List<String> orgIds = user.getOrganizations();

        List<OrgSearchDTO> organizations = new ArrayList<>();

        // organization 조회 -> organization DTO로 변환
        orgIds.forEach(orgId -> {
            organizationRepository.findById(orgId).ifPresent(organization -> {
                OrgSearchDTO orgSearchDTO = OrgSearchDTO.fromEntity(organization);
                organizations.add(orgSearchDTO);
            });
        });

        // 반환
        return ResponseEntity.ok().body(organizations);




    }
}
