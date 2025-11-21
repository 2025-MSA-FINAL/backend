package com.popspot.popupplatform.service.auth;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CoolSMS(누리고) 를 이용한 휴대폰 인증 서비스
 * - 인증번호 발급 + 문자 발송
 * - 인증번호 검증
 * - (간단 구현) 서버 메모리에 인증정보 저장
 */
@Service
public class PhoneVerificationService {

    /** CoolSMS SDK 메시지 전송 서비스 */
    private final DefaultMessageService messageService;

    /** 발신번호 */
    private final String fromNumber;

    /** phone -> 인증정보 매핑 (간단 구현: 서버 메모리) */
    private final Map<String, VerificationInfo> store = new ConcurrentHashMap<>();

    /** 유효시간(초) – 5분 */
    private static final long EXPIRE_SECONDS = 5 * 60;

    public PhoneVerificationService(
            @Value("${coolsms.api-key}") String apiKey,
            @Value("${coolsms.api-secret}") String apiSecret,
            @Value("${coolsms.from-number}") String fromNumber
    ) {
        this.messageService = NurigoApp.INSTANCE.initialize(
                apiKey,
                apiSecret,
                "https://api.coolsms.co.kr"
        );
        this.fromNumber = fromNumber;
    }

    /**
     * 인증번호 생성 + 문자 발송
     */
    public void sendVerificationCode(String phone) {

        // 6자리 랜덤 숫자 생성
        String code = String.format("%06d",
                ThreadLocalRandom.current().nextInt(0, 1_000_000));

        // 문자 내용
        String text = "[Popspot] 인증번호는 " + code + " 입니다. 5분 이내에 입력해 주세요.";

        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(phone);
        message.setText(text);

        try {
            // CoolSMS로 발송
            this.messageService.send(message);
        } catch (Exception e) {
            // 실제 운영에서는 CustomException / 로그 처리 권장
            throw new RuntimeException("문자 발송에 실패했습니다.", e);
        }

        // 메모리에 인증정보 저장 (코드 + 만료시간)
        store.put(phone, new VerificationInfo(
                code,
                Instant.now().plusSeconds(EXPIRE_SECONDS)
        ));
    }

    /**
     * 인증번호 검증
     * @return true: 성공, false: 실패
     */
    public boolean verifyCode(String phone, String code) {
        VerificationInfo info = store.get(phone);
        if (info == null) {
            return false;
        }

        // 만료 여부 체크
        if (info.expiredAt().isBefore(Instant.now())) {
            store.remove(phone);
            return false;
        }

        // 코드 일치 여부 체크
        if (!info.code().equals(code)) {
            return false;
        }

        // ✅ 성공 시 Map에서 바로 제거해서 메모리 누수 방지
        store.remove(phone);
        return true;
    }

    /**
     * 내부용 인증정보 레코드
     */
    private record VerificationInfo(
            String code,
            Instant expiredAt
    ) {}
}
