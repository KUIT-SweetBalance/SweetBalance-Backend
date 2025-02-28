package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "health check API")
public class HealthCheckController {
    @GetMapping("/")
    public ResponseEntity<?> getHealthCheck(@AuthenticationPrincipal UserIdHolder userIdHolder) {
        return ResponseEntity.ok(DefaultResponseDTO.success("Health Check", null));
    }
}
