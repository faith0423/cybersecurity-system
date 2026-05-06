package com.example.cybersecurity.service;

import com.example.cybersecurity.dto.IncidentAnalysisResponse;
import com.example.cybersecurity.exception.ResourceNotFoundException;
import com.example.cybersecurity.model.Incident;
import com.example.cybersecurity.repository.IncidentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final LogService logService;
    private final NotificationEmailService notificationEmailService;
    private final AiAnalysisService aiAnalysisService;
    private final AiService aiService;

    public IncidentService(
            IncidentRepository incidentRepository,
            LogService logService,
            NotificationEmailService notificationEmailService,
            AiAnalysisService aiAnalysisService,
            AiService aiService

    ) {
        this.incidentRepository = incidentRepository;
        this.logService = logService;
        this.notificationEmailService = notificationEmailService;
        this.aiAnalysisService = aiAnalysisService;
        this.aiService = aiService;
    }

    public Incident getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        autoAssignIfDue(incident);

        if (!canAccessIncident(incident)) {
            throw new AccessDeniedException("You are not allowed to view this incident.");
        }

        return incident;
    }

    public List<Incident> getAllIncidents() {
        List<Incident> incidents;
        String role = getCurrentRole();

        if ("ADMIN".equals(role)) {
            incidents = incidentRepository.findAll();
        } else {
            incidents = incidentRepository.findByAssignedRole(role);
        }

        for (Incident incident : incidents) {
            autoAssignIfDue(incident);
        }

        return incidents;
    }

    public Incident createIncident(Incident incident) {
        String role = getCurrentRole();

        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only admin users can create incidents.");
        }

        Map<String, String> aiResult = aiService.analyzeIncident(
                incident.getTitle(),
                incident.getDescription()
        );
        

        incident.setSeverity(aiResult.get("severity"));
        incident.setCategory(aiResult.get("category"));
        incident.setAssignedRole(aiResult.get("assignedRole"));
        incident.setRecommendation(aiResult.get("recommendation"));
        incident.setStatus("SUBMITTED");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            incident.setCreatedBy(authentication.getName());
        }

        Incident savedIncident = incidentRepository.save(incident);
        
        // PREDICTIVE ANALYSIS - Calculate escalation risk
        Map<String, Object> prediction = predictEscalationRisk(savedIncident);
        savedIncident.setPredictedRiskLevel((String) prediction.get("riskLevel"));
        savedIncident.setPredictedRiskScore((Integer) prediction.get("riskScore"));
        savedIncident.setPredictedTimeframe((String) prediction.get("predictedTimeframe"));
        
        Incident updatedIncident = incidentRepository.save(savedIncident);
        
        logService.logAction(1L, "AI created incident " + updatedIncident.getId() + 
                             " with risk prediction: " + prediction.get("riskLevel") + 
                             " (Score: " + prediction.get("riskScore") + ")");

        return updatedIncident;
    }

    public Map<String, Object> predictEscalationRisk(Incident incident) {
        Map<String, Object> prediction = new HashMap<>();
        
        String text = ((incident.getTitle() == null ? "" : incident.getTitle()) + " " + 
                       (incident.getDescription() == null ? "" : incident.getDescription())).toLowerCase();
        
        // Simple scoring algorithm (0-100)
        int riskScore = 0;
        
        // KEYWORD SCORING - High risk keywords
        if (containsAny(text, "urgent", "critical", "emergency", "immediate", "severe", "priority")) {
            riskScore += 30;
        }
        if (containsAny(text, "breach", "ransomware", "data leak", "compromised", "hacked", "intrusion")) {
            riskScore += 40;
        }
        if (containsAny(text, "network", "server", "database", "all users", "entire system")) {
            riskScore += 15;
        }
        if (containsAny(text, "phishing", "scam", "fraud", "suspicious", "malware", "virus")) {
            riskScore += 20;
        }
        
        // DESCRIPTION LENGTH SCORING - Longer descriptions may indicate more complex issues
        if (incident.getDescription() != null && incident.getDescription().length() > 300) {
            riskScore += 10;
        } else if (incident.getDescription() != null && incident.getDescription().length() > 100) {
            riskScore += 5;
        }
        
        // TITLE LENGTH SCORING - Very short titles may lack detail
        if (incident.getTitle() != null && incident.getTitle().length() < 20) {
            riskScore += 5;
        }
        
        // SEVERITY BASED SCORING
        if (incident.getSeverity() != null) {
            switch (incident.getSeverity().toUpperCase()) {
                case "CRITICAL":
                    riskScore += 50;
                    break;
                case "HIGH":
                    riskScore += 30;
                    break;
                case "MEDIUM":
                    riskScore += 15;
                    break;
                case "LOW":
                    riskScore += 5;
                    break;
                default:
                    riskScore += 0;
            }
        }
        
        // ASSIGNED ROLE BASED SCORING - Some roles handle more critical incidents
        if (incident.getAssignedRole() != null) {
            switch (incident.getAssignedRole().toUpperCase()) {
                case "IT_SECURITY":
                    riskScore += 10;
                    break;
                case "NETWORK_SUPPORT":
                    riskScore += 5;
                    break;
                default:
                    riskScore += 0;
            }
        }
        
        // Cap at 100
        riskScore = Math.min(riskScore, 100);
        
        // Determine prediction based on risk score
        String riskLevel;
        String recommendation;
        String predictedTimeframe;
        String actionRequired;
        
        if (riskScore >= 70) {
            riskLevel = "HIGH";
            recommendation = "Immediate attention required. Escalate to senior security team immediately.";
            predictedTimeframe = "Within 4 hours";
            actionRequired = "Urgent: Assign to senior specialist and notify management";
        } else if (riskScore >= 40) {
            riskLevel = "MEDIUM";
            recommendation = "Monitor closely. Review and update status within 2 hours.";
            predictedTimeframe = "Within 12 hours";
            actionRequired = "Priority: Regular monitoring required";
        } else if (riskScore >= 15) {
            riskLevel = "LOW";
            recommendation = "Standard handling. Follow normal incident response procedures.";
            predictedTimeframe = "Within 24 hours";
            actionRequired = "Normal: Follow standard process";
        } else {
            riskLevel = "VERY LOW";
            recommendation = "Low priority. Handle during regular operations.";
            predictedTimeframe = "Within 48 hours";
            actionRequired = "Informational: No immediate action needed";
        }
        
        prediction.put("riskScore", riskScore);
        prediction.put("riskLevel", riskLevel);
        prediction.put("recommendation", recommendation);
        prediction.put("predictedTimeframe", predictedTimeframe);
        prediction.put("actionRequired", actionRequired);
        prediction.put("assessmentDate", LocalDateTime.now().toString());
        
        return prediction;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public Incident updateIncidentStatus(Long id, String newStatus) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        autoAssignIfDue(incident);

        if (!canAccessIncident(incident)) {
            throw new AccessDeniedException("You are not allowed to manage this incident.");
        }

        String role = getCurrentRole();
        if ("ADMIN".equals(role)) {
            throw new AccessDeniedException("Admin can view incidents, but specialist users must manage status updates.");
        }

        String currentStatus = incident.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        incident.setStatus(newStatus);
        Incident savedIncident = incidentRepository.save(incident);

        String resolvedBy = getCurrentUserEmail();

        if ("SOLVED".equalsIgnoreCase(newStatus.trim())) {
            notificationEmailService.sendIncidentSolvedNotification(savedIncident, resolvedBy);
        }

        logService.logAction(1L, "Updated incident " + id + " status to " + newStatus);
        return savedIncident;
    }

    public void deleteIncident(Long id) {
        String role = getCurrentRole();

        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only admin users can delete incidents.");
        }

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        incidentRepository.delete(incident);
        logService.logAction(1L, "Deleted incident with id " + id);
    }

    private void autoAssignIfDue(Incident incident) {
        if (incident.getCreatedAt() == null) {
            return;
        }

        if ("SUBMITTED".equals(incident.getStatus())) {
            LocalDateTime triggerTime = incident.getCreatedAt().plusSeconds(10);
            if (LocalDateTime.now().isAfter(triggerTime) || LocalDateTime.now().isEqual(triggerTime)) {
                incident.setStatus("ASSIGNED");
                incidentRepository.save(incident);
            }
        }
    }

    private boolean isValidTransition(String currentStatus, String newStatus) {
        if ("ASSIGNED".equals(currentStatus) && "FIXING".equals(newStatus)) {
            return true;
        }
        if ("FIXING".equals(currentStatus) && "SOLVED".equals(newStatus)) {
            return true;
        }
        return false;
    }

    private String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "";
        }

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        return authority.startsWith("ROLE_") ? authority.substring(5) : authority;
    }

    private boolean canAccessIncident(Incident incident) {
        String role = getCurrentRole();
        return "ADMIN".equals(role) || role.equals(incident.getAssignedRole());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "Unknown";
        }
        return authentication.getName();
    }

    public Incident getIncidentWithPrediction(Long id) {
        Incident incident = getIncidentById(id);
        if (incident.getPredictedRiskScore() == null) {
            Map<String, Object> prediction = predictEscalationRisk(incident);
            incident.setPredictedRiskLevel((String) prediction.get("riskLevel"));
            incident.setPredictedRiskScore((Integer) prediction.get("riskScore"));
            incident.setPredictedTimeframe((String) prediction.get("predictedTimeframe"));
            incidentRepository.save(incident);
        }
        return incident;
    }
}
