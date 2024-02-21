package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Block.dto.ContentDTO;
import com.Backend.shareNote.domain.Block.service.BlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompPageController {

    private final BlockService blockService;
    private final RabbitTemplate rabbitTemplate;
    //우리가 사용할 exchange 이름
    private final static String CHAT_EXCHANGE_NAME = "chat.exchange";

    @MessageMapping("share.enter.{routingKey}") //enter키로 블록 만들 때 실행
    public void sendText(ContentDTO content, @DestinationVariable String routingKey) {

        // 생성 시간 추가, BaseEntity를 사용하면 밑의 코드를 수행 안해도 돼
//        blockInit.setCreateTime(String.valueOf(System.currentTimeMillis()));

        log.info("routingKey : " + routingKey);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, content);

        content.setRoutingKey(routingKey);

        //block 생성 로직 builder로 만들자
        blockService.createBlock(content);


    }



    // 페이지 입장 시 조회 로직
}
