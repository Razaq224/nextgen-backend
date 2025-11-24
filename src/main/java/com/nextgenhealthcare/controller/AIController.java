package com.nextgenhealthcare.controller;

import com.nextgenhealthcare.dto.SymptomCheckRequest;
import com.nextgenhealthcare.dto.SymptomCheckResponse;
import com.nextgenhealthcare.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    @PostMapping("/symptom-check")
    public ResponseEntity<SymptomCheckResponse> checkSymptoms(@RequestBody SymptomCheckRequest request) {
        if (request.getSymptoms() == null || request.getSymptoms().trim().isEmpty()) {
            SymptomCheckResponse errorResponse = new SymptomCheckResponse();
            errorResponse.setPossibleConditions(Arrays.asList("Invalid input"));
            errorResponse.setRecommendation("Please provide symptoms description.");
            errorResponse.setSuggestedSpecialization("General Medicine");
            errorResponse.setConfidence(0.0);
            errorResponse.setExtractedSymptoms(Collections.emptyList());
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        SymptomCheckResponse response = aiService.checkSymptoms(request.getSymptoms());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        boolean isAvailable = aiService.isServiceAvailable();
        Map<String, String> response = new HashMap<>();
        if (isAvailable) {
            response.put("status", "healthy");
            response.put("aiService", "available");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "unhealthy");
            response.put("aiService", "unavailable");
            return ResponseEntity.status(503).body(response);
        }
    }
}

