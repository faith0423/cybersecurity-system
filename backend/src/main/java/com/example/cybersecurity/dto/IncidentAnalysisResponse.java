package com.example.cybersecurity.dto;

public class IncidentAnalysisResponse {
    private String predictedSeverity;
    private String predictedCategory;
    private String recommendedRole;
    private String recommendation;
    private boolean aiSuccess;
    private String errorMessage;

    public IncidentAnalysisResponse() {
    }

    public IncidentAnalysisResponse(String predictedSeverity,
                                    String predictedCategory,
                                    String recommendedRole,
                                    String recommendation,
                                    boolean aiSuccess,
                                    String errorMessage) {
        this.predictedSeverity = predictedSeverity;
        this.predictedCategory = predictedCategory;
        this.recommendedRole = recommendedRole;
        this.recommendation = recommendation;
        this.aiSuccess = aiSuccess;
        this.errorMessage = errorMessage;
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

    public String getRecommendedRole() {
        return recommendedRole;
    }

    public void setRecommendedRole(String recommendedRole) {
        this.recommendedRole = recommendedRole;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public boolean isAiSuccess() {
        return aiSuccess;
    }

    public void setAiSuccess(boolean aiSuccess) {
        this.aiSuccess = aiSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}