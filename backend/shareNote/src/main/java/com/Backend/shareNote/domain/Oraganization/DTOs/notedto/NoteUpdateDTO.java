package com.Backend.shareNote.domain.Oraganization.DTOs.notedto;

import lombok.Data;

@Data
public class NoteUpdateDTO {
    private String title;
    private String noteImageUrl;

    private String organizationId;
    private String noteId;
}
