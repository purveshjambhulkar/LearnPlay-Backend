package com.example.LNPbackend.controller;

import com.example.LNPbackend.model.User;
import com.example.LNPbackend.payloads.UserDto;
import com.example.LNPbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8081")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
        try {
            UserDto createdUser = this.userService.createUser(userDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserDto user = this.userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "User not found",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/progress")
    public ResponseEntity<?> addTopicProgress(
            @PathVariable Long userId,
            @RequestParam String topicName,
            @RequestParam int score,
            @RequestParam String category) {
        try {
            UserDto updatedUser = userService.addTopicProgress(userId, topicName, score, category);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to update progress",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(Authentication authentication) {
        try {
            List<Map<String, Object>> leaderboard = userService.getLeaderboard();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to fetch leaderboard",
                    "message", e.getMessage()
            ));
        }
    }

    // Add to UserController.java
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(Authentication authentication) {
        try {
            String username = (String) authentication.getPrincipal();
            Map<String, Object> dashboardData = userService.getDashboardData(username);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to fetch dashboard data",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/game-result")
    public ResponseEntity<?> addGameResult(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        try {
            String username = (String) authentication.getPrincipal();
            UserDto updatedUser = userService.addGameResult(
                    username,
                    (String) payload.get("topic"),
                    (Integer) payload.get("score"),
                    (Integer) payload.get("totalQuestions")
            );
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to save game result",
                    "message", e.getMessage()
            ));
        }
    }
    @GetMapping("/")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = this.userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Map<String, Object> authResponse = userService.verify(user);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials", "message", "Username or password is incorrect"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed", "message", e.getMessage()));
        }
    }
}