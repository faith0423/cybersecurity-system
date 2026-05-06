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
        logService.logAction(1L, "AI created incident " + savedIncident.getId());

        return savedIncident;
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
public Map<String, Object> predictEscalationRisk(Incident incident) {
    Map<String, Object> prediction = new HashMap<>();
    
    String text = ((incident.getTitle() == null ? "" : incident.getTitle()) + " " + 
                   (incident.getDescription() == null ? "" : incident.getDescription())).toLowerCase();
    
    // Simple scoring algorithm
    int riskScore = 0;
    
    // Keyword scoring
    if (containsAny(text, "urgent", "critical", "emergency", "immediate", "severe")) {
        riskScore += 30;
    }
    if (containsAny(text, "breach", "ransomware", "data leak", "compromised")) {
        riskScore += 40;
    }
    if (containsAny(text, "network", "server", "database", "all users")) {
        riskScore += 15;
    }
    
    // Length scoring (longer descriptions might be more serious)
    if (incident.getDescription() != null && incident.getDescription().length() > 200) {
        riskScore += 10;
    }
    
    // Severity based scoring
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
        }
    }
    
    // Cap at 100
    riskScore = Math.min(riskScore, 100);
    
    // Determine prediction
    String riskLevel;
    String recommendation;
    String predictedTimeframe;
    
    if (riskScore >= 70) {
        riskLevel = "HIGH";
        recommendation = "Immediate attention required. Escalate to senior security team.";
        predictedTimeframe = "4 hours";
    } else if (riskScore >= 40) {
        riskLevel = "MEDIUM";
        recommendation = "Monitor closely. Review within 2 hours.";
        predictedTimeframe = "12 hours";
    } else {
        riskLevel = "LOW";
        recommendation = "Standard handling. Review within 24 hours.";
        predictedTimeframe = "24 hours";
    }
    
    prediction.put("riskScore", riskScore);
    prediction.put("riskLevel", riskLevel);
    prediction.put("recommendation", recommendation);
    prediction.put("predictedTimeframe", predictedTimeframe);
    
    return prediction;
}

// Helper method for containsAny (if not already in this class)
private boolean containsAny(String text, String... keywords) {
    for (String keyword : keywords) {
        if (text.contains(keyword)) return true;
    }
    return false;
}

}