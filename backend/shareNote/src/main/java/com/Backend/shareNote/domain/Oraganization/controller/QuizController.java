package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.DTOs.quizdto.QuizCreateDTO;
import com.Backend.shareNote.domain.Oraganization.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QuizController {
    private final QuizService quizService;
    @PostMapping("/quiz")
    public ResponseEntity<Object> createQuiz(@RequestBody QuizCreateDTO quizCreateDTO){
        return quizService.createQuiz(quizCreateDTO);
    }
}
