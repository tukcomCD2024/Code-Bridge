package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.DTOs.quizdto.QuizCreateDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.quizdto.QuizSearchDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.quizdto.QuizSolveDTO;
import com.Backend.shareNote.domain.Oraganization.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QuizController {
    private final QuizService quizService;
    @PostMapping("/quiz")
    public ResponseEntity<Object> createQuiz(@RequestBody QuizCreateDTO quizCreateDTO){
        return quizService.createQuiz(quizCreateDTO);
    }

    @PostMapping("/quiz-solutions")
    public ResponseEntity<Object> createQuizSolutions(@RequestBody QuizSolveDTO quizCreateDTO){
        return quizService.createQuizSolutions(quizCreateDTO);
    }

    @GetMapping("/quiz/{organizationId}/{noteId}/{userId}")
    public ResponseEntity<?> getQuiz(@PathVariable String organizationId, @PathVariable String noteId, @PathVariable String userId){
        return quizService.getQuiz(organizationId, noteId, userId);
    }
}
