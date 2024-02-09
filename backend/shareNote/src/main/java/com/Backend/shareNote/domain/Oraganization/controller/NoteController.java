package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.notedto.NoteCreateDTO;
import com.Backend.shareNote.domain.Oraganization.notedto.NoteDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.notedto.NoteUpdateDTO;
import com.Backend.shareNote.domain.Oraganization.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoteController {
    private final NoteService noteService;
    @PostMapping("/user/note")
    public String createNote(@RequestBody NoteCreateDTO noteCreateDTO){
        return noteService.createNote(noteCreateDTO);

    }

    @DeleteMapping("/user/note")
    public String deleteNote(@RequestBody NoteDeleteDTO noteDeleteDTO){
        return noteService.deleteNote(noteDeleteDTO);
    }

    @GetMapping("/user/note/{organizationId}")
    public List<Organization.Note> getNote(@PathVariable String organizationId){
        return noteService.getNotes(organizationId);
    }

    @PatchMapping("/user/note")
    public String updateNote(@RequestBody NoteUpdateDTO noteUpdateDTO){
        return noteService.updateNote(noteUpdateDTO);

    }
}
