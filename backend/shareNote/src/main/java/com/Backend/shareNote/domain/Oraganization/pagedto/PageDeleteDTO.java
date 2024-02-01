package com.Backend.shareNote.domain.Oraganization.pagedto;

import lombok.Data;
import org.springframework.web.bind.annotation.RestController;

@Data
public class PageDeleteDTO {
    private String organizationId;
    private String noteId;
    private String pageId;
}
