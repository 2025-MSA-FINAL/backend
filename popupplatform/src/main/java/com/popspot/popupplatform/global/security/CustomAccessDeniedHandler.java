// src/main/java/com/popspot/popupplatform/global/security/CustomAccessDeniedHandler.java
package com.popspot.popupplatform.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.global.exception.ErrorResponse;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponse body = new ErrorResponse(
                AuthErrorCode.ACCESS_DENIED.getCode(),
                AuthErrorCode.ACCESS_DENIED.getMessage()
        );

        response.setStatus(AuthErrorCode.ACCESS_DENIED.getHttpStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
