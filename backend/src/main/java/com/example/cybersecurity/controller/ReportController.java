package com.example.cybersecurity.controller;

import com.example.cybersecurity.service.NotificationEmailService;
import com.example.cybersecurity.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost:4201",
        "http://localhost:63404"
})
public class ReportController {

    private final ReportService reportService;
    private final NotificationEmailService notificationEmailService;

    public ReportController(ReportService reportService, NotificationEmailService notificationEmailService) {
        this.reportService = reportService;
        this.notificationEmailService = notificationEmailService;
    }

    @GetMapping("/incidents/pdf")
    public ResponseEntity<byte[]> downloadIncidentPdfReport() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            throw new AccessDeniedException("You are not authorized to download this report.");
        }

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        String role = authority.startsWith("ROLE_") ? authority.substring(5) : authority;

        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only admin users can download reports.");
        }

        byte[] pdfBytes = reportService.generateIncidentPdfReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=incident-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    @PostMapping("/incidents/pdf/email")
    public ResponseEntity<String> emailIncidentPdfReport() throws Exception {
        String generatedBy = requireAdminAndGetEmail();
        byte[] pdfBytes = reportService.generateIncidentPdfReport();
        notificationEmailService.sendIncidentReportPdf(pdfBytes, generatedBy);
        return ResponseEntity.ok("PDF report emailed successfully.");
    }
    private String requireAdminAndGetEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            throw new AccessDeniedException("You are not authorized.");
        }

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        String role = authority.startsWith("ROLE_") ? authority.substring(5) : authority;

        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only admin users can access reports.");
             }

        return authentication.getName();
    }
}
