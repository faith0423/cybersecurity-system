package com.example.cybersecurity.controller;

import com.example.cybersecurity.model.Incident;
import com.example.cybersecurity.service.IncidentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost:4201",
        "http://localhost:63404"
})
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Incident> getAll() {
        return service.getAllIncidents();
    }

    @GetMapping("/{id}")
    public Incident getById(@PathVariable Long id) {
        return service.getIncidentById(id);
    }

    @PostMapping
    public Incident create(@RequestBody Incident incident) {
        return service.createIncident(incident);
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return service.updateIncidentStatus(id, payload.get("status"));
    }

   @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
    service.deleteIncident(id);
    return ResponseEntity.noContent().build();
}
}