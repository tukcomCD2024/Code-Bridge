package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.DTOs.quizdto.QuizCreateDTO;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.NoteRepository;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Oraganization.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final OrganizationRepository organizationRepository;
    private final NoteRepository noteRepository;
    private final QuizRepository quizRepository;

    public ResponseEntity<Object> createQuiz(QuizCreateDTO quizCreateDTO) {
        try {
            // organization 찾기
            Organization organization = organizationRepository.findById(quizCreateDTO.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 organization이 없습니다."));

            // note 찾기
            Organization.Note note = organization.getNotes().stream()
                    .filter(n -> n.getId().equals(quizCreateDTO.getNoteId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 note가 없습니다."));

            // quiz 생성
            Organization.Quiz quiz = Organization.Quiz.builder()
                    .quizType(quizCreateDTO.getQuizType())
                    .problem(quizCreateDTO.getProblem())
                    .answer(quizCreateDTO.getAnswer())
                    .solutions(quizCreateDTO.getSolutions())
                    .correctUser(new ArrayList<String>())
                    .wrongUser(new ArrayList<String>())
                    .userId(quizCreateDTO.getUserId())
                    .build();

            // organization 멤버인지 확인하는 코드
            if (!organization.getMembers().contains(quizCreateDTO.getUserId())) {
                throw new IllegalArgumentException("해당하는 organization의 멤버가 아닙니다.");
            }

            quizRepository.save(quiz);

            // note에 quiz 추가
            note.getQuiz().add(quiz);

            return ResponseEntity.ok().build();
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }
}
