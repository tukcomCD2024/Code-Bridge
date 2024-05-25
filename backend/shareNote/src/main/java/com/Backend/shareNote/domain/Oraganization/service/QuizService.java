package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.DTOs.quizdto.*;
import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import com.Backend.shareNote.domain.Oraganization.repository.QuizRepository;
import com.Backend.shareNote.domain.User.entity.Users;
import com.Backend.shareNote.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final OrganizationRepository organizationRepository;

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    @Transactional
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

            Users user = userRepository.findById(quizCreateDTO.getUserId()).get();
            // quiz 생성
            Organization.Quiz quiz = Organization.Quiz.builder()
                    .quizType(quizCreateDTO.getQuizType())
                    .problem(quizCreateDTO.getProblem())
                    .answer(quizCreateDTO.getAnswer())
                    .problems(quizCreateDTO.getProblems())
                    .correctUser(new ArrayList<String>())
                    .wrongUser(new ArrayList<String>())
                    .userId(quizCreateDTO.getUserId())
                    .nickname(user.getNickname())
                    .quizTitle(quizCreateDTO.getQuizTitle())
                    .build();



            // 퀴즈를 note에 추가
            note.getQuiz().add(quiz);

            // organization 멤버인지 확인하는 코드
            if (!organization.getMembers().contains(quizCreateDTO.getUserId())) {
                throw new IllegalArgumentException("해당하는 organization의 멤버가 아닙니다.");
            }

            quizRepository.save(quiz);



            organizationRepository.save(organization);

            return ResponseEntity.ok().body("퀴즈 생성 성공");
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }
    @Transactional
    public ResponseEntity<Object> createQuizSolutions(QuizSolveDTO quizCreateDTO) {
        try {
            // organization 찾기
            Organization organization = organizationRepository.findById(quizCreateDTO.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 organization이 없습니다."));

            // note 찾기
            Organization.Note note = organization.getNotes().stream()
                    .filter(n -> n.getId().equals(quizCreateDTO.getNoteId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 note가 없습니다."));

            // quiz 찾기
            Organization.Quiz quiz = note.getQuiz().stream()
                    .filter(q -> q.getId().equals(quizCreateDTO.getQuizId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 quiz가 없습니다."));

            // organization 멤버인지 확인하는 코드
            if (!organization.getMembers().contains(quizCreateDTO.getUserId())) {
                throw new IllegalArgumentException("해당하는 organization의 멤버가 아닙니다.");
            }

            // 자기가 낸 문제 푸는 경우
            if (quiz.getUserId().equals(quizCreateDTO.getUserId())) {
                throw new IllegalArgumentException("자신이 낸 문제는 풀 수 없습니다.");
            }

            // CorrectUser, WrongUser에 userId가 있으면 이미 풀었다는 뜻
            // 이미 푼 문제에 대해서 제출을 시도할 때
            if (quiz.getCorrectUser().contains(quizCreateDTO.getUserId()) ||
                    quiz.getWrongUser().contains(quizCreateDTO.getUserId())) {
                throw new IllegalArgumentException("이미 풀었습니다.");
            }

            // 정답 확인
            if (quiz.getAnswer() == quizCreateDTO.getAnswer()) {
                quiz.getCorrectUser().add(quizCreateDTO.getUserId());
                organizationRepository.save(organization);
                return ResponseEntity.ok().body("퀴즈 정답!!");
            } else {
                quiz.getWrongUser().add(quizCreateDTO.getUserId());
                organizationRepository.save(organization);
                return ResponseEntity.ok().body("퀴즈 실패!! 정답은 " + quiz.getAnswer() + "입니다");
            }

        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }


    public ResponseEntity<?> getQuiz(String organizationId, String noteId, String userId) {
        try {
            // organization 찾기
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 organization이 없습니다."));

            // note 찾기
            Organization.Note note = organization.getNotes().stream()
                    .filter(n -> n.getId().equals(noteId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 note가 없습니다."));
            List<QuizSearchDTO> quizList = new ArrayList<>();


            note.getQuiz().stream().filter(quiz -> !quiz.getUserId().equals(userId)).forEach(quiz -> {
                QuizSearchDTO quizSearchDTO = new QuizSearchDTO();
                quizSearchDTO.setQuizId(quiz.getId());
                quizSearchDTO.setQuizTitle(quiz.getProblem());
                quizSearchDTO.setNickname(quiz.getNickname());
                if(quiz.getCorrectUser().contains(userId)){
                    quizSearchDTO.setCorrect(1);
                }
                else if(quiz.getWrongUser().contains(userId)){
                    quizSearchDTO.setCorrect(0);
                }
                else{
                    quizSearchDTO.setCorrect(-1);
                }
                quizList.add(quizSearchDTO);
            });

            // quiz 찾기
            return ResponseEntity.ok().body(quizList);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }

    }

    public ResponseEntity<?> getQuizDetail(QuizDetailReqDTO quizDetailReqDTO) {
        try {
            // organization 찾기
            Organization organization = organizationRepository.findById(quizDetailReqDTO.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 organization이 없습니다."));

            // note 찾기
            Organization.Note note = organization.getNotes().stream()
                    .filter(n -> n.getId().equals(quizDetailReqDTO.getNoteId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 note가 없습니다."));

            // quiz 찾기
            Organization.Quiz quiz = note.getQuiz().stream()
                    .filter(q -> q.getId().equals(quizDetailReqDTO.getQuizId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 quiz가 없습니다."));

            //
            int corret;
            if(quiz.getCorrectUser().contains(quizDetailReqDTO.getUserId())){
                corret = 1;
            }
            else if(quiz.getWrongUser().contains(quizDetailReqDTO.getUserId())){
                corret = 0;
            }
            else{
                corret = -1;
            }

            QuizDetailResDTO dto = QuizDetailResDTO.builder()
                    .quizTitle(quiz.getProblem())
                    .quizType(quiz.getQuizType())
                    .noteName(note.getTitle())
                    .problems(quiz.getProblems())
                    .correct(corret)
                    .build();

            return ResponseEntity.ok().body(dto);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> deleteQuiz(QuizDeleteDTO quizDeleteDTO) {
        try {
            // organization 찾기
            Organization organization = organizationRepository.findById(quizDeleteDTO.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 organization이 없습니다."));

            // note 찾기
            Organization.Note note = organization.getNotes().stream()
                    .filter(n -> n.getId().equals(quizDeleteDTO.getNoteId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 note가 없습니다."));

            // quiz 찾기
            Organization.Quiz quiz = note.getQuiz().stream()
                    .filter(q -> q.getId().equals(quizDeleteDTO.getQuizId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 quiz가 없습니다."));

            // organization 멤버인지 확인하는 코드
            if (!organization.getMembers().contains(quizDeleteDTO.getUserId())) {
                throw new IllegalArgumentException("해당하는 organization의 멤버가 아닙니다.");
            }

            // 자기가 낸 문제인 경우만 삭제 가능
            if (quiz.getUserId().equals(quizDeleteDTO.getUserId())) {
                note.getQuiz().remove(quiz);
                organizationRepository.save(organization);
            } else{
                throw new IllegalArgumentException("자신이 낸 문제만 삭제 가능합니다.");
            }

            return ResponseEntity.ok().body("퀴즈 삭제 성공");
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }
}
