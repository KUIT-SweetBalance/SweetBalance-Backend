package com.sweetbalance.backend.util;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class InnerFilterResponseSender {

    public static <T> void sendInnerResponse(HttpServletResponse response, int status, int code, String message, T data) {

        try{
            DefaultResponseDTO<T> defaultResponseDTO = new DefaultResponseDTO<>(status, code, message, data);
            sendResponse(response, defaultResponseDTO);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // 실제 응답을 전송하는 메소드
    private static void sendResponse(HttpServletResponse response, DefaultResponseDTO<?> defaultResponseDTO) throws IOException {
        response.setStatus(defaultResponseDTO.getStatus());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(defaultResponseDTO.toString());
    }
}
