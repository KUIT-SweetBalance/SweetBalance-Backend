package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO){
        try {

            userService.join(signUpRequestDTO);
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("회원가입 성공", null)
            );
        } catch (RuntimeException e) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "중복된 email")
            );
        }
    }

    @PostMapping("/email-duplicate")
    public ResponseEntity<?> emailDuplicateCheck(@RequestBody Map<String, String> requestBody){
        String email = requestBody.get("email");

        Optional<User> user = userService.findUserByEmailAndLoginType(email, LoginType.BASIC);
        if(user.isPresent()) {
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.of(200, 1, "이미 사용중인 email 입니다.", null)
            );
        }
        else{
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.of(200, 0, "사용 가능한 email 입니다.", null)
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