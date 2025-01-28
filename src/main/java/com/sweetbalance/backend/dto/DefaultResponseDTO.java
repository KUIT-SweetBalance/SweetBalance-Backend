package com.sweetbalance.backend.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DefaultResponseDTO<T> {
    private int status;

    private int code;

    private String message;

    private T data;

    public static <T> DefaultResponseDTO<T> success(String message, T data) {
        return new DefaultResponseDTO<>(200, 0, message, data);
    }

    // todo 특정한 data가 응답에 포함되지 않을 경우, null이 아닌 다른 방식으로의 응답 요함 (응답 규칙 정의 후 반영)
    public static <T> DefaultResponseDTO<T> error(int status, int code, String message) {
        return new DefaultResponseDTO<>(status, code, message, null);
    }

    public static <T> DefaultResponseDTO<T> of(int status, int code, String message, T data) {
        return new DefaultResponseDTO<>(status, code, message, data);
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 형식으로 변환 중 문제 발생", e);
        }
    }
}