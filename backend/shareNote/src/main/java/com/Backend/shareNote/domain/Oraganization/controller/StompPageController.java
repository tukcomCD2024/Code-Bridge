package com.Backend.shareNote.domain.Oraganization.controller;

import com.Backend.shareNote.domain.Block.dto.ShareDTO;
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

    private final RabbitTemplate rabbitTemplate;
    //우리가 사용할 exchange 이름
    private final static String CHAT_EXCHANGE_NAME = "chat.exchange";

    @MessageMapping("send.share.{routingKey}")
    public void sendText(ShareDTO share, @DestinationVariable String routingKey) {

        // 생성 시간 추가
        share.setCreateTime(String.valueOf(System.currentTimeMillis()));

        log.info("routingKey : " + routingKey);
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, share);

        share.setRoutingKey(routingKey);

        //해당 내용을 블록에 저장하는 로직 작성!! 

    }
}
