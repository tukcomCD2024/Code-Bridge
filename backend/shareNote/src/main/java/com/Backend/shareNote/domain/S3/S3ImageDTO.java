package com.Backend.shareNote.domain.S3;

import lombok.Getter;

@Getter
public class S3ImageDTO {
    private String image_url;

    public S3ImageDTO(String image_url) {
        this.image_url = image_url;
    }
}
