package com.example.LNPbackend.payloads;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {
    // Basic user info
    private String username;
    private String email;
    private String password;
    private LocalDateTime joinDate;

    // Stats and progression
    private int totalScore;
    private String tier;  // Calculated based on totalScore
    private int topicsLearnedCount;

    // Recent topics data (last 4 topics)
    private List<RecentTopicDto> recentTopics;

    // All topics learned (simplified view)
    private List<String> allTopicsLearned;

    @Data
    public static class RecentTopicDto {
        private String topicName;
        private int score;
        private LocalDateTime completionDate;
        private String category;

        public RecentTopicDto(String topicName, int score, LocalDateTime completionDate, String category) {
            this.topicName = topicName;
            this.score = score;
            this.completionDate = completionDate;
            this.category = category;
        }
    }
}