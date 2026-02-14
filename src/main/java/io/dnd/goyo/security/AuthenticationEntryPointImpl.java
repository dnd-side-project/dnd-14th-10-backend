package io.dnd.goyo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.exception.ErrorResponse;
import io.dnd.goyo.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ErrorCode errorCode = resolveErrorCode(request);
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private ErrorCode resolveErrorCode(HttpServletRequest request) {
        Object jwtError = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE);
        if (jwtError instanceof ErrorCode errorCode) {
            return errorCode;
        }
        return ErrorCode.UNAUTHORIZED;
    }
}
