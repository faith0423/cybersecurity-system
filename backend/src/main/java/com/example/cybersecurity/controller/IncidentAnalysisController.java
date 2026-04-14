package com.example.cybersecurity.controller;

import com.example.cybersecurity.dto.IncidentAnalysisResponse;
import com.example.cybersecurity.service.AiAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost:4201",
        "http://localhost:63404"
})
public class IncidentAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    public IncidentAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

   @PostMapping("/analyze")
public ResponseEntity<IncidentAnalysisResponse> analyze(@RequestBody Map<String, String> payload) {

    String title = payload.get("title");
    String description = payload.get("description");

    IncidentAnalysisResponse response = aiAnalysisService.analyzeIncident(title, description);

    return ResponseEntity.ok(response);
}
}