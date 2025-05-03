package com.example.LNPbackend.service.impl;

import com.example.LNPbackend.model.User;
import com.example.LNPbackend.payloads.UserDto;
import com.example.LNPbackend.repository.UserRepo;
import com.example.LNPbackend.service.JWTService;
import com.example.LNPbackend.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        // Hash the password properly
        user.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        user.setJoinDate(LocalDateTime.now());
        user.setTotalScore(0);

        User savedUser = userRepo.save(user);
        return this.userToDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return this.userToDto(user);
    }
    @Override
    public List<Map<String, Object>> getLeaderboard() {
        List<User> users = userRepo.findAllByOrderByTotalScoreDesc();

        return users.stream()
                .limit(100) // Limit to top 100 users
                .map(user -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("rank", users.indexOf(user) + 1);
                    entry.put("username", user.getUsername());
                    entry.put("totalScore", user.getTotalScore());
                    entry.put("topicsCompleted", user.getTopicsLearned().size());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserByUsername(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return this.userToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::userToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepo.delete(user);
    }

    @Override
    public Map<String, Object> getDashboardData(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Calculate rank
        List<User> allUsers = userRepo.findAllByOrderByTotalScoreDesc();
        int rank = allUsers.indexOf(user) + 1;

        // Prepare recent activities (last 4 topics)
        List<Map<String, Object>> recentActivities = user.getRecentTopicScores().stream()
                .sorted(Comparator.comparing(User.TopicScore::getDateLearned).reversed())
                .limit(4)
                .map(topic -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("topic", topic.getTopicName());
                    activity.put("score", topic.getScore());
                    activity.put("date", topic.getDateLearned());
                    activity.put("accuracy", calculateAccuracy(topic.getScore(), 10));
                    return activity;
                })
                .collect(Collectors.toList());

        // Calculate average accuracy
        double avgAccuracy = user.getRecentTopicScores().stream()
                .limit(4)
                .mapToDouble(topic -> calculateAccuracy(topic.getScore(), 10))
                .average()
                .orElse(0);


        Map<String, Object> response = new HashMap<>();
        response.put("totalScore", user.getTotalScore());
        response.put("rank", rank);
        response.put("topicsCompleted", user.getTopicsLearned().size());
        response.put("averageAccuracy", avgAccuracy);
        response.put("recentActivities", recentActivities);

        return response;
    }
    private double calculateAccuracy(int score, int totalQuestions) {
        return ((double) score / (totalQuestions * 10)) * 100; // 10 points per question
    }


    @Override
    public UserDto addGameResult(String username, String topic, int score, int totalQuestions) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Add to topics learned if not already there
        if (!user.getTopicsLearned().contains(topic)) {
            user.getTopicsLearned().add(topic);
        }

        // Add to recent topic scores (using your existing method)
        user.addLearnedTopic(topic, score, "game");

        // Update total score
        user.setTotalScore(user.getTotalScore() + score);

        User updatedUser = userRepo.save(user);
        return userToDto(updatedUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepo.save(user);
        return this.userToDto(updatedUser);
    }


    @Override
    public Map<String, Object> verify(User user) throws BadCredentialsException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                User authenticatedUser = userRepo.findByUsername(user.getUsername());
                String token = jwtService.generateToken(user.getUsername());

                return Map.of(
                        "token", token,
                        "username", authenticatedUser.getUsername(),
                        "tier", authenticatedUser.getTier(),
                        "totalScore", authenticatedUser.getTotalScore(),
                        "topicsLearnedCount", authenticatedUser.getTopicsLearned().size()
                );
            } else {
                throw new BadCredentialsException("Invalid credentials");
            }
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials", e);
        }
    }

    @Override
    public UserDto addTopicProgress(Long userId, String topicName, int score, String category) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // This would call the addLearnedTopic method from your User entity
        user.addLearnedTopic(topicName, score, category);

        User updatedUser = userRepo.save(user);
        return this.userToDto(updatedUser);
    }

    private UserDto userToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(""); // Don't expose password
        userDto.setJoinDate(user.getJoinDate());
        userDto.setTotalScore(user.getTotalScore());
        userDto.setTier(user.getTier());
        userDto.setTopicsLearnedCount(user.getTopicsLearned().size());

        // Map recent topics
        List<UserDto.RecentTopicDto> recentTopics = user.getRecentTopicScores().stream()
                .limit(4)
                .map(topic -> new UserDto.RecentTopicDto(
                        topic.getTopicName(),
                        topic.getScore(),
                        topic.getDateLearned(),
                        "default" // Set default category or add to TopicScore
                ))
                .collect(Collectors.toList());
        userDto.setRecentTopics(recentTopics);

        // Map all topics learned
        userDto.setAllTopicsLearned(new ArrayList<>(user.getTopicsLearned()));

        return userDto;
    }
}