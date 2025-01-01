package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> findUserById(@PathVariable("userId") Long userId) {
        // 아래 로직을 통해 토큰의 username 값을 Controller 로직에서 가져올 수 있음. role 값도 가능
        UserIdHolder userIdHolder = (UserIdHolder) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(userIdHolder.getUserId()+" requested "+userId+"'s INFO");

        Optional<User> userOptional = userService.findUserByUserId(userId);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{userId}/beverages")
    public ResponseEntity<List<Beverage>> getBeveragesByUserId(@PathVariable("userId") Long userId) {
        List<Beverage> beverages = userService.findBeveragesByUserId(userId);
        return ResponseEntity.ok(beverages);
    }

    @GetMapping("/username")
    public ResponseEntity<User> findUserById(@RequestParam("username") String username) {
        Optional<User> userOptional = userService.findUserByUsername(username);

        if (userOptional.isPresent()) return ResponseEntity.ok(userOptional.get());
        else                          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
