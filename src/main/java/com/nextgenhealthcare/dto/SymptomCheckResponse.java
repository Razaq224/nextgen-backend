package com.nextgenhealthcare.dto;

import lombok.Data;
import java.util.List;

@Data
public class SymptomCheckResponse {
    private List<String> possibleConditions;
    private String recommendation;
    private String suggestedSpecialization;
    private Double confidence;
    private List<String> extractedSymptoms;
}



