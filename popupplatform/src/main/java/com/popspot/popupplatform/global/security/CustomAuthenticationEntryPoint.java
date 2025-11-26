// src/main/java/com/popspot/popupplatform/global/security/CustomAuthenticationEntryPoint.java
package com.popspot.popupplatform.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.global.exception.ErrorResponse;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.global.exception.code.BaseErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object attr = request.getAttribute("authErrorCode");
        BaseErrorCode errorCode;

        if (attr instanceof BaseErrorCode) {
            errorCode = (BaseErrorCode) attr;
        } else {
            // JwtAuthenticationFilter에서 아무 코드도 안 심어줬다면 토큰이 아예 없는 케이스로 처리
            errorCode = AuthErrorCode.UNKNOWN_AUTH_ERROR;
        }

        ErrorResponse body = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
