package com.popspot.popupplatform.global.geo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kakao.local")
public class KakaoLocalProperties {

    /**
     * 예: https://dapi.kakao.com
     */
    private String baseUrl;

    /**
     * Kakao REST API Key
     */
    private String restApiKey;
}
