package com.example.LNPbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyDy355m1zjIa09WBWQh5-kQG-PX8aiJ7rk";
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Map<String, Object> askAI(String topic, List<String> selectedQuestionTypes) throws Exception {
        String prompt = buildPrompt(topic, selectedQuestionTypes);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topP", 0.9,
                        "maxOutputTokens", 2048
                )
        );

        String response = webClient.post()
                .uri(geminiApiUrl)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return processResponse(response);
    }

    private String buildPrompt(String topic, List<String> selectedQuestionTypes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a quiz about '").append(topic).append("' with the following question types:\n\n");

        // Add specific instructions for each question type
        for (String type : selectedQuestionTypes) {
            switch (type) {
                case "fill-blank":
                    prompt.append("- Fill-in-the-blank questions (key: fillInTheBlanks):\n");
                    prompt.append("  - Provide 5 questions\n");
                    prompt.append("  - Each question should have:\n");
                    prompt.append("    - 'sentence': The sentence with blanks marked as ____\n");
                    prompt.append("    - 'answer': The correct word(s) to fill in\n");
                    prompt.append("    - 'difficulty': easy/medium/hard\n\n");
                    break;

                case "matching":
                    prompt.append("- Matching questions (key: matchThePairs):\n");
                    prompt.append("  - Provide 5 matching sets\n");
                    prompt.append("  - Each set should have:\n");
                    prompt.append("    - 'pairs': Array of 4-5 left-right pairs\n");
                    prompt.append("    - 'difficulty': easy/medium/hard\n\n");
                    break;

                case "true-false":
                    prompt.append("- True/False questions (key: trueFalse):\n");
                    prompt.append("  - Provide 5 statements\n");
                    prompt.append("  - Each should have:\n");
                    prompt.append("    - 'statement': The factual statement\n");
                    prompt.append("    - 'answer': boolean true/false (without quotes)\n");
                    prompt.append("    - 'difficulty': easy/medium/hard\n\n");
                    break;

                case "order":
                    prompt.append("- Sequencing questions (key: sequence):\n");
                    prompt.append("  - Provide 5 ordering challenges\n");
                    prompt.append("  - Each should have:\n");
                    prompt.append("    - 'options': Shuffled items to order\n");
                    prompt.append("    - 'correctOrder': Correct sequence (0-based indices)\n");
                    prompt.append("    - 'difficulty': easy/medium/hard\n\n");
                    break;

                case "multiple-choice":
                    prompt.append("- Multiple Choice questions (key: mcq):\n");
                    prompt.append("  - Provide 5 questions\n");
                    prompt.append("  - Each should have:\n");
                    prompt.append("    - 'question': The question text\n");
                    prompt.append("    - 'options': Array of 4 options\n");
                    prompt.append("    - 'answer': Correct option (exact text match)\n");
                    prompt.append("    - 'difficulty': easy/medium/hard\n\n");
                    break;
            }
        }

        prompt.append("Format requirements:\n");
        prompt.append("- Return ONLY valid JSON format\n");
        prompt.append("- Use the exact keys specified above for each section\n");
        prompt.append("- For true/false answers, use boolean values (true/false without quotes)\n");
        prompt.append("- Do not include any explanations or markdown formatting\n");
        prompt.append("- Example structure:\n");
        prompt.append("{\n");
        prompt.append("  \"trueFalse\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"statement\": \"TypeScript is a superset of JavaScript\",\n");
        prompt.append("      \"answer\": true,\n");
        prompt.append("      \"difficulty\": \"easy\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}");

        return prompt.toString();
    }

    private Map<String, Object> processResponse(String response) throws Exception {
        JsonNode rootNode = objectMapper.readTree(response);
        String jsonContent = rootNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText()
                .replace("```json", "")
                .replace("```", "")
                .trim();

        return objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
    }
}