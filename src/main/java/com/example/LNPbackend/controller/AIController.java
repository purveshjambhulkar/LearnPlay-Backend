package com.example.LNPbackend.controller;

import com.example.LNPbackend.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "http://localhost:8081",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    @Autowired
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askAI(@RequestBody Map<String, Object> payload) throws Exception {
        String topic = (String) payload.get("topic");
        List<String> selectedQuestionTypes = (List<String>) payload.get("selectedQuestionTypes");

        Map<String, Object> response = aiService.askAI(topic, selectedQuestionTypes);
        return ResponseEntity.ok(response);
    }
}
