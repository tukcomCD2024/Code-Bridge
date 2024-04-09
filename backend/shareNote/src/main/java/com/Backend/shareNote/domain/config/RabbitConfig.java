package com.Backend.shareNote.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@RequiredArgsConstructor
@Configuration //여기서 정의된 빈들이 스프링 컨테이너에서 관리된대
@EnableRabbit //@RabbitListener를 사용하게 해줌
public class RabbitConfig {


    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private static final String ROUTING_KEY = "room.*";

    // Queue 등록
    //여기서 true는 서버를 재시작해도 queue가 유지되도록 하는 것(진짜??)
    @Bean
    public Queue queue() {
        return new Queue(CHAT_QUEUE_NAME, true);
    }


    // Exchange 등록
    // TopicExchange 는 라우팅 키를 기반으로 메세지를 큐로 라우팅하는 방식
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(CHAT_EXCHANGE_NAME);
    }


    // Exchange 와 Queue 바인딩
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
    }


    @Bean
    public  com.fasterxml.jackson.databind.Module dateTimeModule() {
        return new JavaTimeModule();
    }


    // Spring 에서 자동생성해주는 ConnectionFactory 는 SimpleConnectionFactory
    // 여기서 사용하는 건 CachingConnectionFactory 라 새로 등록해줌
    // RabbitMQ 와 연결하기 위한 ConnectionFactory
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost("rabbitmq"); //로컬에서 실행 시 localhost 도커로 실행 시 RABBIT_HOST 환경 변수인 rabbitmq 로 설정
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }

    /**
     * messageConverter를 커스터마이징 하기 위해 Bean 새로 등록
     */
    // RabbitMQ로 메세지를 보내기 위한 템플릿
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setRoutingKey(CHAT_QUEUE_NAME);
        return rabbitTemplate;
    }

    // RabbitMQ로 메세지를 받기 위한 리스너관리
    // 지정된 큐로부터 메시지를 비동기적으로 수신하기 위한 컨테이너 설정(수신 느낌)
    @Bean
    public SimpleMessageListenerContainer myContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(CHAT_QUEUE_NAME);

        return container;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        //LocalDateTime serializable 을 위해
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.registerModule(dateTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

}