package com.Backend.shareNote.domain.Oraganization.DTOs.pagedto;

import lombok.Data;

@Data
public class PageCreateDTO {
    private String organizationId;
    private String noteId;
    private String createUserId;
}
