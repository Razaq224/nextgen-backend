package com.nextgenhealthcare.service;

import com.nextgenhealthcare.dto.SymptomCheckRequest;
import com.nextgenhealthcare.dto.SymptomCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

@Service
public class AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final WebClient webClient;
    
    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;
    
    public AIService(WebClient.Builder webClientBuilder, @Value("${ai.service.url:http://localhost:5000}") String configuredUrl) {
        // Ensure the URL is available at construction time
        this.aiServiceUrl = configuredUrl;
        this.webClient = webClientBuilder
                .baseUrl(this.aiServiceUrl)
                .build();
        logger.info("AIService initialized with URL: {}", this.aiServiceUrl);
    }
    
    public SymptomCheckResponse checkSymptoms(String symptoms) {
        logger.info("Checking symptoms: {}", symptoms.substring(0, Math.min(50, symptoms.length())));
        
        // First check if service is available
        if (!isServiceAvailable()) {
            logger.error("AI service is not available at {}", aiServiceUrl);
            return createErrorResponse(
                "AI service is not available",
                "Please ensure the AI service is running on port 5000. Start it with: cd ai-service && python app.py"
            );
        }
        
        try {
            SymptomCheckRequest request = new SymptomCheckRequest();
            request.setSymptoms(symptoms);
            
            logger.info("Sending request to AI service: {}/api/symptom-check", aiServiceUrl);
            
            SymptomCheckResponse response = webClient.post()
                    .uri("/api/symptom-check")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SymptomCheckResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                            .filter(throwable -> !(throwable instanceof WebClientResponseException)))
                    .block();
            
            if (response == null) {
                logger.error("Received null response from AI service");
                return createErrorResponse(
                    "No response from AI service",
                    "The AI service did not return a valid response. Please try again."
                );
            }
            
            logger.info("Successfully received response from AI service");
            return response;
            
        } catch (WebClientResponseException e) {
            logger.error("Error from AI service: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            String errorMessage = "AI service error";
            String recommendation = "Please try again later or consult with a healthcare professional.";
            
            if (e.getStatusCode().value() == 503) {
                errorMessage = "AI service is not ready";
                recommendation = "The AI service is still initializing. Please wait a moment and try again.";
            } else if (e.getStatusCode().value() == 500) {
                errorMessage = "Error processing symptoms";
                recommendation = "There was an error processing your symptoms. Please try again with a different description.";
            }
            
            return createErrorResponse(errorMessage, recommendation);
            
        } catch (Exception e) {
            logger.error("Unexpected error calling AI service: {}", e.getMessage(), e);
            return createErrorResponse(
                "Connection error",
                String.format("Cannot connect to AI service at %s. Please ensure it is running: cd ai-service && python app.py", aiServiceUrl)
            );
        }
    }
    
    private SymptomCheckResponse createErrorResponse(String condition, String recommendation) {
        SymptomCheckResponse errorResponse = new SymptomCheckResponse();
        errorResponse.setPossibleConditions(Arrays.asList(condition));
        errorResponse.setRecommendation(recommendation);
        errorResponse.setSuggestedSpecialization("General Medicine");
        errorResponse.setConfidence(0.0);
        errorResponse.setExtractedSymptoms(Collections.emptyList());
        return errorResponse;
    }
    
    public boolean isServiceAvailable() {
        try {
            String response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            boolean available = response != null && response.contains("healthy");
            logger.info("AI service health check: {}", available ? "available" : "unavailable");
            return available;
        } catch (Exception e) {
            logger.warn("AI service health check failed: {}", e.getMessage());
            return false;
        }
    }
}

