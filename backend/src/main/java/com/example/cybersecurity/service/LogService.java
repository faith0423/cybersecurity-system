package com.example.cybersecurity.service;

import com.example.cybersecurity.model.Log;
import com.example.cybersecurity.repository.LogRepository;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void logAction(Long userId, String action) {
        Log log = new Log();
        log.setAction(action);
        logRepository.save(log);
    }
}