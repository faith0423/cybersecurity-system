package com.example.cybersecurity.dto;

public class AiAnalysisResponse {

    private String predictedSeverity;
    private String predictedCategory;
    private String recommendation;
    private String assignedRole;

    public AiAnalysisResponse() {
    }

    public AiAnalysisResponse(String predictedSeverity, String predictedCategory, String recommendation, String assignedRole) {
        this.predictedSeverity = predictedSeverity;
        this.predictedCategory = predictedCategory;
        this.recommendation = recommendation;
        this.assignedRole = assignedRole;
    }

    public String getPredictedSeverity() {
        return predictedSeverity;
    }

    public void setPredictedSeverity(String predictedSeverity) {
        this.predictedSeverity = predictedSeverity;
    }

    public String getPredictedCategory() {
        return predictedCategory;
    }

    public void setPredictedCategory(String predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAssignedRole() {
        return assignedRole;
    }

    public void setAssignedRole(String assignedRole) {
        this.assignedRole = assignedRole;
    }
}