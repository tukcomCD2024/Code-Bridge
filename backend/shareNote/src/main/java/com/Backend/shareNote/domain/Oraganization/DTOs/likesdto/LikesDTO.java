package com.Backend.shareNote.domain.Oraganization.DTOs.likesdto;

import lombok.Data;

@Data
public class LikesDTO {
    private String organizationId;
    private String noteId;
    private String lover;
    private String blockId;
    private String heartReceiver;
}
