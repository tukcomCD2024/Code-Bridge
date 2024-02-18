package com.Backend.shareNote.domain.Block.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShareDTO {
    private String type;
    private String content;
    private int blockNum;
    private String createUser;
    private String createTime;
    private String nickName;
    private String crdtIndex; // 타입은 뭔지 모르겠네 아마 float?
    private String routingKey;

}
