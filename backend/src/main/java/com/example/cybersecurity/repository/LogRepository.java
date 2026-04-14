package com.example.cybersecurity.repository;

import com.example.cybersecurity.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {
}