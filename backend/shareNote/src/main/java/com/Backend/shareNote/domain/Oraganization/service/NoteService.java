package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.notedto.NoteCreateDTO;
import com.Backend.shareNote.domain.Oraganization.notedto.NoteDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.notedto.NoteUpdateDTO;
import com.Backend.shareNote.domain.Oraganization.repository.NoteRepository;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {
    private final NoteRepository noteRepository;
    private final OrganizationRepository organizationRepository;
    @Transactional
    public String createNote(NoteCreateDTO noteCreateDTO) {
        // organization 찾기
        Organization organization = organizationRepository.findById(noteCreateDTO.getOrganizationId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 organization이 없습니다."));
        // note 생성
        Organization.Note note = Organization.Note.builder()
                .title(noteCreateDTO.getTitle())
                .createUser(noteCreateDTO.getUserId())
                .pages(new ArrayList<Organization.Page>())
                .noteImageUrl(noteCreateDTO.getNoteImageUrl())
                .build();
        noteRepository.save(note);
        // organization에 note 추가
        organization.getNotes().add(note);
        organizationRepository.save(organization);
        return "노트 생성 성공!";
    }

    @Transactional
    public String deleteNote(NoteDeleteDTO noteDeleteDTO) {
        Organization.Note note = noteRepository.findById(noteDeleteDTO.getNoteId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 note가 없습니다."));
        Organization organization = organizationRepository.findById(noteDeleteDTO.getOrganizationId()).get();
        //organization에서 note 삭제 로직
        // organization에서 note 찾기
        Optional<Organization.Note> optionalNote = organization.getNotes().stream()
                .filter(n -> n.getId().equals(note.getId()))
                .findFirst();

        // 찾은 note가 존재한다면 삭제
        if (optionalNote.isPresent()) {
            organization.getNotes().remove(optionalNote.get());
            organizationRepository.save(organization);
            noteRepository.delete(note);
            return "노트 삭제 성공!";
        } else {
            return "노트를 찾을 수 없습니다.";
        }

    }

    public List<Organization.Note> getNotes(String organizationId) {
        Organization organization = organizationRepository.findById(organizationId).get();
        return organization.getNotes();
    }

    public String updateNote(NoteUpdateDTO noteUpdateDTO) {
        Organization oranization = organizationRepository.findById(noteUpdateDTO.getOrganizationId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 organization이 없습니다."));

        Organization.Note note = noteRepository.findById(noteUpdateDTO.getNoteId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 note가 없습니다."));
        //노트 업데이트
        note.setTitle(noteUpdateDTO.getTitle());
        note.setNoteImageUrl(noteUpdateDTO.getNoteImageUrl());

        //organization에서 note 업데이트
        oranization.getNotes().stream()
                .filter(n -> n.getId().equals(note.getId()))
                .findFirst()
                .ifPresent(n -> {
                    n.setTitle(noteUpdateDTO.getTitle());
                    n.setNoteImageUrl(noteUpdateDTO.getNoteImageUrl());
                });

        noteRepository.save(note);
        organizationRepository.save(oranization);
        return "노트 수정 성공!";
    }
}
