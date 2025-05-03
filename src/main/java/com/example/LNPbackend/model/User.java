package com.example.LNPbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="user_name", nullable = false, length = 100)
    private String username;

    private String email;
    private String password;

    private int totalScore = 0;

    @Transient  // This field will be calculated based on totalScore, not stored directly
    private String tier;
    private LocalDateTime joinDate;


    @ElementCollection
    private List<String> topicsLearned = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateLearned DESC")
    private List<TopicScore> recentTopicScores = new ArrayList<>();

    // Method to calculate tier based on totalScore
    public String getTier() {
        if (totalScore >= 5000) return "Grandmaster";
        if (totalScore >= 4500) return "Master";
        if (totalScore >= 4200) return "Diamond III";
        if (totalScore >= 3900) return "Diamond II";
        if (totalScore >= 3600) return "Diamond I";
        if (totalScore >= 3300) return "Platinum III";
        if (totalScore >= 3000) return "Platinum II";
        if (totalScore >= 2700) return "Platinum I";
        if (totalScore >= 2400) return "Gold III";
        if (totalScore >= 2100) return "Gold II";
        if (totalScore >= 1800) return "Gold I";
        if (totalScore >= 1500) return "Silver III";
        if (totalScore >= 1200) return "Silver II";
        if (totalScore >= 900) return "Silver I";
        if (totalScore >= 600) return "Bronze III";
        if (totalScore >= 300) return "Bronze II";
        return "Bronze I";
    }

    public void addLearnedTopic(String topicName, int score, String category) {
        // If topic is already present, just update recent scores
        if (!topicsLearned.contains(topicName)) {
            if (topicsLearned.size() >= 4) {
                // Remove the oldest topic
                String removedTopic = topicsLearned.remove(0);

                // Optionally remove its related scores from recentTopicScores
                // (if desired — or you can leave them if they're part of history)
                recentTopicScores.removeIf(scoreEntry -> scoreEntry.getTopicName().equals(removedTopic));
            }

            topicsLearned.add(topicName);
        }

        // Add new topic score to the top
        recentTopicScores.add(0, new TopicScore(topicName, score, LocalDateTime.now(), category));

        // Limit to 4 most recent score entries
        if (recentTopicScores.size() > 4) {
            while (recentTopicScores.size() > 4) {
                recentTopicScores.remove(recentTopicScores.size() - 1); // Remove the last element
            }

        }

        totalScore += score;
    }


    @Entity
    @Data
    @NoArgsConstructor
    public static class TopicScore {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String topicName;
        private int score;
        private LocalDateTime dateLearned;
        private String category; // Add this field

        public TopicScore(String topicName, int score, LocalDateTime dateLearned, String category) {
            this.topicName = topicName;
            this.score = score;
            this.dateLearned = dateLearned;
            this.category = category;
        }
    }
}