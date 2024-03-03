package com.Backend.shareNote.domain.Oraganization.organdto;

import com.Backend.shareNote.domain.Oraganization.entity.Organization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class OrgSearchDTO {

    private String id;
    private String name;
    private String description;
    private String owner;
    private String emoji;
    private List<String> members;
    private List<NoteDto> notes;

    // 내부 클래스로 Note DTO 정의
    @Getter
    @Setter
    @NoArgsConstructor
    public static class NoteDto {
        private String id;
        private String title;
        private String noteImageUrl;
        private List<PageDto> pages;
    }

    // 내부 클래스로 Page DTO 정의
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PageDto {
        private String id;
        private List<String> blocks; // 블록의 ID만 포함
    }

    // Organization 엔티티를 DTO로 변환하는 메서드
    public static OrgSearchDTO fromEntity(Organization organization) {
        OrgSearchDTO dto = new OrgSearchDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());
        dto.setDescription(organization.getDescription());
        dto.setOwner(organization.getOwner());
        dto.setEmoji(organization.getEmoji());
        dto.setMembers(organization.getMembers());
        dto.setNotes(organization.getNotes().stream()
                .map(note -> {
                    NoteDto noteDto = new NoteDto();
                    noteDto.setId(note.getId());
                    noteDto.setTitle(note.getTitle());
                    noteDto.setNoteImageUrl(note.getNoteImageUrl());
                    noteDto.setPages(note.getPages().stream()
                            .map(page -> {
                                PageDto pageDto = new PageDto();
                                pageDto.setId(page.getId());
                                pageDto.setBlocks(page.getBlocks());
                                return pageDto;
                            })
                            .collect(Collectors.toList()));
                    return noteDto;
                })
                .collect(Collectors.toList()));
        return dto;
    }
}
