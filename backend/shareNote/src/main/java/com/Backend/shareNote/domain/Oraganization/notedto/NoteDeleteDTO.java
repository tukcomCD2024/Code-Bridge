package com.Backend.shareNote.domain.Oraganization.notedto;

import lombok.Data;

@Data
public class NoteDeleteDTO {
    private String organizationId;
    private String noteId;
}
