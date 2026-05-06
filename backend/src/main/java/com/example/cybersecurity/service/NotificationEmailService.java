package com.example.cybersecurity.service;

import com.example.cybersecurity.model.Incident;
import com.example.cybersecurity.model.User;
import com.example.cybersecurity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationEmailService {
    @Autowired
    private MailjetEmailService mailjetEmailService;

    @Autowired
    private UserRepository userRepository;

    @Async
    public void sendIncidentSolvedNotification(Incident incident, String resolvedBy) {
        List<User> admins = userRepository.findByRole("ADMIN");
        
        if (admins.isEmpty()) return;

        String subject = "Incident Solved - " + incident.getTitle();
        String body = String.format("""
            <h2>Incident Solved</h2>
            <p><strong>Title:</strong> %s</p>
            <p><strong>Resolved By:</strong> %s</p>
            <p><strong>Date:</strong> %s</p>
            <p><strong>Status:</strong> SOLVED</p>
                """,
                incident.getTitle(),
                resolvedBy,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        for (User admin : admins) {
            mailjetEmailService.sendIncidentSolvedEmail(admin.getEmail(), subject, body);
        }
    }
public void sendIncidentReportPdf(byte[] pdfBytes, String generatedBy) {
        mailjetEmailService.sendPdfReportEmail("amomokoena04@gmail.com", generatedBy, pdfBytes);
    }

    
}