package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.request.EmailVerificationRequestDTO;
import com.sweetbalance.backend.dto.request.ResetPasswordRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        try{
            String email = signUpRequestDTO.getEmail();
            Optional<User> user = userService.findUserByEmailAndLoginTypeAndDeletedAtIsNull(email, LoginType.BASIC);

            if(user.isPresent()) {
                return ResponseEntity.status(400).body(
                        DefaultResponseDTO.error(400, 101, "중복된 email")
                );
            }

            userService.join(signUpRequestDTO);
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("회원가입 성공", null)
            );

        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "회원가입 실패")
            );
        }
    }

    @PostMapping("/email-duplicate")
    public ResponseEntity<?> emailDuplicateCheck(@RequestBody Map<String, String> requestBody){
        String email = requestBody.get("email");

        Optional<User> user = userService.findUserByEmailAndLoginTypeAndDeletedAtIsNull(email, LoginType.BASIC);
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

    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(@AuthenticationPrincipal UserIdHolder userIdHolder){

        Long userId = userIdHolder.getUserId();
        Optional<User> userOptional = userService.findUserByUserId(userId);

        if (userOptional.isPresent()) {
            userService.softDeleteUser(userOptional.get());
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("회원 탈퇴 완료", null)
            );
        } else {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
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

    @PostMapping("/email-verification")
    public ResponseEntity<?> sendEmailVerificationCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 1000, "이메일이 입력되지 않았습니다.")
            );
        }
        if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 1003, "유효하지 않은 이메일 형식입니다.")
            );
        }
        try {
            boolean result = userService.sendEmailVerificationCode(email);
            if(result) {
                return ResponseEntity.status(200).body(
                        DefaultResponseDTO.success("6자리 인증코드가 발송되었습니다.", null)
                );
            } else {
                return ResponseEntity.status(400).body(
                        DefaultResponseDTO.error(400, 1004, "인증코드 발송에 실패했습니다.")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "인증코드 발급 중 오류가 발생했습니다.")
            );
        }
    }

    @PostMapping("/email-verification-code-check")
    public ResponseEntity<?> checkEmailVerificationCode(@RequestBody EmailVerificationRequestDTO request) {
        String email = request.getEmail();
        String code = request.getCode();

        if(email == null || email.isEmpty() || code == null || code.isEmpty()){
            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 1006, "이메일 또는 인증코드가 입력되지 않았습니다.")
            );
        }
        try {
            boolean isValid = userService.checkEmailVerificationCode(email, code);
            if(isValid) {
                return ResponseEntity.status(200).body(
                        DefaultResponseDTO.success("인증코드가 일치합니다.", null)
                );
            } else {
                return ResponseEntity.status(400).body(
                        DefaultResponseDTO.error(400, 1007, "인증코드가 일치하지 않거나 만료되었습니다.")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "인증코드 검증 중 오류가 발생했습니다.")
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        String email = request.getEmail();
        String newPassword = request.getNewPassword();
        if (email == null || email.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 1008, "이메일 또는 새 비밀번호가 입력되지 않았습니다.")
            );
        }
        try {
            boolean result = userService.resetPassword(email, newPassword);
            if (result) {
                return ResponseEntity.status(200).body(
                        DefaultResponseDTO.success("비밀번호가 재설정되었습니다.", null)
                );
            } else {
                return ResponseEntity.status(400).body(
                        DefaultResponseDTO.error(400, 1009, "이메일 인증이 만료되었거나, 유효하지 않은 이메일입니다.")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "비밀번호 재설정 중 오류가 발생했습니다.")
            );
        }
    }

}