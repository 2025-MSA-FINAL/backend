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
            // 인증 문제는 아님
            errorCode = AuthErrorCode.UNKNOWN_AUTH_ERROR;
            // 콘솔에 어떤 에러가 발생했는지 출력
            System.err.println("Authentication failed: "
                    + authException.getClass().getName()
                    + " - " + authException.getMessage());
            authException.printStackTrace(); // 스택트레이스까지 찍어줌

        }

        ErrorResponse body = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
