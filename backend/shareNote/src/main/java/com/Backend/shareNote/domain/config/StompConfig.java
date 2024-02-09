package com.Backend.shareNote.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp/chat") //웹소켓 처음 연결시 사용
                .setAllowedOriginPatterns("http://*.*.*.*:8081", "http://*:8081","http://*:3000") //안해도 무관
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setPathMatcher(new AntPathMatcher("."));  // url을 chat/room/3 -> chat.room.3으로 참조하기 위한 설정
        registry.setApplicationDestinationPrefixes("/pub");

        //registry.enableSimpleBroker("/sub");
        //외부 메세지 브로커와 연결(rabbitMQ)
        //BrokerRelay를 통해 외부 메세지 브로커와 연결
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                //이 위 부분은 subscribe할 때 사용할 수 있는 exchange를 명시한거 같아
                //
                .setRelayHost("rabbitmq")
                .setRelayPort(61613);
    }
}