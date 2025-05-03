package com.example.LNPbackend.service;

import com.example.LNPbackend.model.User;
import com.example.LNPbackend.payloads.UserDto;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.List;
import java.util.Map;

/**
 * Service interface for user management operations
 */
public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserById(Long userId);
    List<UserDto> getAllUsers();
    UserDto updateUser(UserDto userDto, Long userId);
    void deleteUser(Long userId);
    Map<String, Object> verify(User user) throws BadCredentialsException;
    UserDto addTopicProgress(Long userId, String topicName, int score, String category);
    UserDto getUserByUsername(String username);
    UserDto addGameResult(
            String username,
            String topic,
            int score,
            int totalQuestions
    );
    Map<String, Object> getDashboardData(String username);
    List<Map<String, Object>> getLeaderboard();

}