package com.popspot.popupplatform.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

//Redis 채널에서 메시지를 구독(Subscribe) 후
//수신된 메시지 STOMP를 통해 채팅방참여중인 클라이언트들에게 실시간으로 전송
//MessageListener Redis 메시지를 비동기적으로 처리
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageSubscriber implements MessageListener {
    //STOMP 프로토콜을 통해 특정 목적지(/sub/chat/room/{roomId})로
    private final SimpMessagingTemplate messagingTemplate;
    //JSON 문자열로 수신된 Redis 메시지 본문을 Java 객체(DTO)로 역직렬화하는 데 사용
    private final ObjectMapper objectMapper;

    //Redis 채널에서 메시지를 수신했을 때 호출
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try{
            //Redis에서 수신된 메시지 본문(byte[])을 UTF-8 문자열로 디코딩
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            //직렬화된 JSON 문자열을 DTO객체로 역직렬화
            ChatMessageResponse chatMessage = objectMapper.readValue(body,ChatMessageResponse.class);

            //메세지가 전송될 STOMP 목적지 설정
            String destination = "/sub/chat/room/" + chatMessage.getRoomId();

            log.debug("Redis 수신 -> STOMP 브로드캐스트: {}", destination);

            //SimpMessagingTemplate으로 해당 채팅방을 구독 중인 모든 클라이언트에게 메시지 전송
            messagingTemplate.convertAndSend(destination, chatMessage);

        } catch (Exception e) {
            log.error("Redis 메세지 처리 중 오류",e);
        }
    }
}
