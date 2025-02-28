package com.sweetbalance.backend.util.handler;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handle404(NoHandlerFoundException ex) {
        return ResponseEntity.status(200).body(
                DefaultResponseDTO.of(200, 999, "존재하지 않는 엔드포인트입니다.", null)
        );
    }
}