package com.example.cybersecurity.repository;

import com.example.cybersecurity.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByAssignedRole(String assignedRole);
}