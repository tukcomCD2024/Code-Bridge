package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.DTOs.contributiondto.ContributionResultDTO;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ContributionService {
    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    public ResponseEntity<?> getContribution(String organizationId) {
        try {
            // organization 찾기
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 organization이 없습니다."));


            // 우선 organization의 멤버들의 우선 quiz 맞춘게 있으면 +1을 하자
            List<ContributionResultDTO> resultlist = new ArrayList<>();

            HashMap<String, Integer> quizMap = new HashMap<>();
            HashMap<String, Integer> likeMap = new HashMap<>();

            // quizMap 초기화
            organization.getMembers().forEach(member -> {
                quizMap.put(member, 0);
            });

            // 노트 조회 하면서 퀴즈의 맞춘 목록을 뒤지면서 quizMap 완성하기
            organization.getNotes().forEach(note -> {
                //note의 like 정보를 갖고 있는 맵
                Map<String, Set<Organization.BlockLike>> userLikes = note.getLikesInfo().getUserLikes();

                for (String uuid :note.getLikesInfo().getUserLikes().keySet()) {
                    likeMap.put(uuid, likeMap.getOrDefault(uuid,0) + userLikes.get(uuid).size() );
                }


                note.getQuiz().forEach(quiz -> {
                    for (String key : quiz.getCorrectUser()) {
                        quizMap.put(key, quizMap.get(key) + 1);
                    }
                    // 퀴즈 출제자도 1점 추가
                    quizMap.put(quiz.getUserId(), quizMap.get(quiz.getUserId()) + 1);
                });
            });

            organization.getMembers().forEach(member -> {
                // 모든 유저에 대해서 닉네임, id, quizScore, (likeScore)를 넣어주자
                ContributionResultDTO contributionResultDTO = new ContributionResultDTO();
                contributionResultDTO.setUserId(member);

                // 닉네임 추가하기
                userRepository.findById(member).ifPresent(user -> {
                    contributionResultDTO.setNickname(user.getNickname());
                });

                // quiz 점수 추가
                contributionResultDTO.setQuizScore(quizMap.get(member));

                // like 점수 추가 (추후에 추가 예정)
                contributionResultDTO.setLikeScore(likeMap.getOrDefault(member,0));

                resultlist.add(contributionResultDTO);
            });

            return ResponseEntity.ok().body(resultlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
