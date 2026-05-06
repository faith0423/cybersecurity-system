package com.example.cybersecurity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    private String severity;

    private String category;
    private String assignedRole;
    
    private String recommendation;
    private String status;
    private String createdBy;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // PREDICTIVE ANALYSIS FIELDS - Add these
    private String predictedRiskLevel;
    private Integer predictedRiskScore;
    private String predictedTimeframe;

    public Incident() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters for all fields
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAssignedRole() {
        return assignedRole;
    }

    public void setAssignedRole(String assignedRole) {
        this.assignedRole = assignedRole;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // PREDICTIVE ANALYSIS GETTERS AND SETTERS
    public String getPredictedRiskLevel() {
        return predictedRiskLevel;
    }

    public void setPredictedRiskLevel(String predictedRiskLevel) {
        this.predictedRiskLevel = predictedRiskLevel;
    }

    public Integer getPredictedRiskScore() {
        return predictedRiskScore;
    }

    public void setPredictedRiskScore(Integer predictedRiskScore) {
        this.predictedRiskScore = predictedRiskScore;
    }

    public String getPredictedTimeframe() {
        return predictedTimeframe;
    }

    public void setPredictedTimeframe(String predictedTimeframe) {
        this.predictedTimeframe = predictedTimeframe;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}