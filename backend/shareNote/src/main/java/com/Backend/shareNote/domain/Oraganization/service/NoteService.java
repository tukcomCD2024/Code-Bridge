package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import com.Backend.shareNote.domain.Oraganization.exception.SelfLikedException;
import com.Backend.shareNote.domain.Oraganization.DTOs.likesdto.LikesDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.notedto.NoteCreateDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.notedto.NoteDeleteDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.notedto.NoteSearchDTO;
import com.Backend.shareNote.domain.Oraganization.DTOs.notedto.NoteUpdateDTO;
import com.Backend.shareNote.domain.Oraganization.repository.NoteRepository;
import com.Backend.shareNote.domain.Oraganization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<NoteSearchDTO> createNote(NoteCreateDTO noteCreateDTO) {
        // organization 찾기
        Organization organization = organizationRepository.findById(noteCreateDTO.getOrganizationId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 organization이 없습니다."));
        // note 생성
        Organization.Note note = Organization.Note.builder()
                .title(noteCreateDTO.getTitle())
                .createUser(noteCreateDTO.getUserId())
                .pages(new ArrayList<Organization.Page>())
                .noteImageUrl(noteCreateDTO.getNoteImageUrl())
                .likesInfo(new Organization.LikesInfo())
                .quiz(new ArrayList<Organization.Quiz>())
                .build();
        noteRepository.save(note);
        // organization에 note 추가
        organization.getNotes().add(note);
        organizationRepository.save(organization);

        NoteSearchDTO noteSearchDTO = new NoteSearchDTO();
        noteSearchDTO.setNoteId(note.getId());
        return ResponseEntity.ok(noteSearchDTO);

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
        Organization organization = organizationRepository.findById(noteUpdateDTO.getOrganizationId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 organization이 없습니다."));

        Organization.Note note = noteRepository.findById(noteUpdateDTO.getNoteId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 note가 없습니다."));
        //노트 업데이트
        note.setTitle(noteUpdateDTO.getTitle());
        note.setNoteImageUrl(noteUpdateDTO.getNoteImageUrl());

        //organization에서 note 업데이트
        organization.getNotes().stream()
                .filter(n -> n.getId().equals(note.getId()))
                .findFirst()
                .ifPresent(n -> {
                    n.setTitle(noteUpdateDTO.getTitle());
                    n.setNoteImageUrl(noteUpdateDTO.getNoteImageUrl());
                });

        noteRepository.save(note);
        organizationRepository.save(organization);
        return "노트 수정 성공!";
    }

    public ResponseEntity<String> blockLikes(LikesDTO likesDTO) {
        // organization 찾기
        Organization organization = organizationRepository.findById(likesDTO.getOrganizationId())
                .orElseThrow(()->new IllegalArgumentException("해당하는 organization이 없습니다."));

        ResponseEntity<String> responseEntity = null;

        try {
            // note 찾기 및 좋아요 처리
            Optional<Organization.Note> noteOptional = organization.getNotes().stream()
                    .filter(n -> n.getId().equals(likesDTO.getNoteId()))
                    .findFirst();

            if (noteOptional.isPresent()) {
                // 노트가 존재해
                if(noteOptional.get().getLikesInfo().addLike(likesDTO.getHeartReceiver(), likesDTO.getBlockId(), likesDTO.getLover())){
                    responseEntity = ResponseEntity.ok("좋아요 성공!");
                } else {
                    responseEntity = ResponseEntity.ok("좋아요 취소!");
                }
            } else {
                return ResponseEntity.badRequest().body("해당하는 노트가 없습니다.");
            }
        } catch (SelfLikedException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        organizationRepository.save(organization);
        return responseEntity;
    }
}
