package com.Backend.shareNote.domain.Block.dto;

import lombok.Data;

import java.util.List;

@Data
public class BlockDTO {
    private String id;
    private String type;
    private List<ContentDTO> content;
    private int blockSeq;
    private String createUser;
    // 필요한 추가 필드

    // Content DTO 정의
    @Data
    public static class ContentDTO {
        private String type;
        private String content;
        private String blockId;
        private int blockSeq;
        private String nickName;
        private String crdtIndex;
        // 필요한 추가 필드
    }
}
