package com.Backend.shareNote.domain.Oraganization.notedto;

import lombok.Data;

@Data
public class NoteCreateDTO {
    private String organizationId;
    private String title;
    private String userId;
    private String noteImageUrl;
}
