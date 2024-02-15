package com.Backend.shareNote.domain.S3;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final S3UploadService s3UploadService;
    @PostMapping("/api/image")
    public S3ImageDTO uploadReview(@RequestParam("multipartFile") MultipartFile multipartFile) throws IOException {
        S3ImageDTO s3ImageDto = new S3ImageDTO(s3UploadService.saveFile(multipartFile));
        return s3ImageDto;

    }

}
