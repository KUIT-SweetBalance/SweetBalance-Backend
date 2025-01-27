package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignUpController {

    private final UserService userService;

    @Autowired
    public SignUpController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/api/auth/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO){
        try {

            userService.join(signUpRequestDTO);
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("회원가입 성공", null)
            );
        } catch (RuntimeException e) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "중복된 username 또는 email")
            );
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String errorMessage = "잘못된 입력 형식입니다.";
        if (e.getMessage().contains("Gender")) {
            errorMessage = "성별은 'MALE' 또는 'FEMALE'만 가능합니다.";
        }
        return ResponseEntity.status(400).body(
                DefaultResponseDTO.error(400, 999, errorMessage)
        );
    }
}