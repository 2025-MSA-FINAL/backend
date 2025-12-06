package com.popspot.popupplatform.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    //클라이언트가 연결할 WebSocket EndPoint
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") //전체허용
                .withSockJS(); //SockJs 지원
    }
    //STOMP 메시지 라우팅 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registy) {
        //클라이언트가 메시지 보낼 prefix (SEND)
        registy.setApplicationDestinationPrefixes("/pub");
        //서버가 클라이언트에게 push해줄 prefix (SUBSCRIBE)
        registy.enableSimpleBroker("/sub");
    }
}
