package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.entity.User;
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
                    DefaultResponseDTO.error(400, 999, "중복된 username 또는 email")
            );
        }
    }

    @PostMapping("/id-duplicate")
    public ResponseEntity<?> idDuplicateCheck(@RequestBody Map<String, String> requestBody){
        String username = requestBody.get("username");

        Optional<User> user = userService.findUserByUsername(username);
        if(user.isPresent()) {
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.of(200, 1, "이미 사용중인 username 입니다.", null)
            );
        }
        else{
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.of(200, 0, "사용 가능한 username 입니다.", null)
            );
        }
    }

    @PostMapping("/re-password")
    public ResponseEntity<?> rePassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 1000, "이메일이 입력되지 않았습니다.")
            );
        }

        try {
            boolean result = userService.sendTemporaryPassword(email);

            if(result) {
                return ResponseEntity.status(200).body(
                        DefaultResponseDTO.success("임시 비밀번호가 발송되었습니다.", null)
                );
            } else {
                return ResponseEntity.status(400).body(
                        DefaultResponseDTO.error(400, 1001, "해당 이메일을 가진 사용자를 찾을 수 없습니다.")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 1002, "임시 비밀번호 발급 중 오류가 발생했습니다.")
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