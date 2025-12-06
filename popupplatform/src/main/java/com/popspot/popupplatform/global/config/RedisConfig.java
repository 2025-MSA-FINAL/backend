package com.popspot.popupplatform.global.config;

import com.popspot.popupplatform.infra.redis.ChatMessageSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    //채팅 메시지용 Redis Topic
    @Bean
    public ChannelTopic chatTopic() {
        return new ChannelTopic("chat_message_topic");
    }
    //RedisTemplate key는 문자열, value는 JSON 형식의 객체로 직렬화하여 저장하도록 설정
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Key 직렬화 (String)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // Value 직렬화 (객체->JSON)
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 일반 키-값 직렬화 설정
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        // Hash 타입의 키-값 직렬화 설정
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);
        // RedisTemplate의 설정을 반영 (특정 초기화 작업)
        template.afterPropertiesSet();
        return template;
    }

    //connectionFactory 데이터베이스 연결의 DataSource와 유사하게, Redis 서버의 주소, 포트, 인증 정보 등 실제 연결 정보를 포함하고 있는 객체
    //Redis Pub/Sub Listener 설정
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory, //Redis 연결 팩토리
            ChatMessageSubscriber chatMessageSubscriber, //Redis 메시지 수신 시 처리 로직을 담고 있는 구독자
            ChannelTopic chatTopic //구독할 채널 토픽
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        //connectionFactory가 제공하는 연결을 사용하여 메시지 수신
        container.setConnectionFactory(connectionFactory);
        // 정의된 구독자(Subscriber)와 토픽(Topic)을 리스너 컨테이너에 등록
        container.addMessageListener(chatMessageSubscriber, chatTopic);
        return container;
    }
}
