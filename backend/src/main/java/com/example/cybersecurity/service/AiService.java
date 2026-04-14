package com.example.cybersecurity.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    public Map<String, String> analyzeIncident(String title, String description) {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase();

        String category;
        String severity;
        String assignedRole;
        String recommendation;

        // ── CATEGORY & ROLE DETECTION ──────────────────────────────────────────

        if (containsAny(text, "phish", "scam", "spam", "fake email", "suspicious email",
                "malicious link", "credential", "password reset", "urgent action",
                "click here", "verify your account", "account suspended")) {

            category       = "Phishing / Scam Email";
            assignedRole   = "IT_SECURITY";
            recommendation = "Do not click any links. Report the email to IT Security, block the sender, "
                           + "and reset credentials if any were entered. Conduct phishing awareness training.";
            severity = containsAny(text, "credential", "password", "login", "account")
                       ? "HIGH" : "MEDIUM";

        } else if (containsAny(text, "ransomware", "malware", "virus", "trojan", "spyware",
                "infected", "worm", "keylogger", "rootkit", "exploit", "breach",
                "data leak", "exfiltrat", "unauthorised access", "unauthorized access",
                "hacked", "intrusion", "compromised system")) {

            category       = "Security Compromise";
            assignedRole   = "IT_SECURITY";
            recommendation = "Immediately isolate the affected system from the network. "
                           + "Preserve logs, notify IT Security, and initiate incident response procedures. "
                           + "Conduct a full forensic analysis.";
            severity = containsAny(text, "ransomware", "breach", "exfiltrat", "data leak", "hacked")
                       ? "CRITICAL" : "HIGH";

        } else if (containsAny(text, "network", "internet", "connectivity", "vpn", "wifi",
                "wi-fi", "no connection", "disconnected", "slow network", "packet loss",
                "firewall", "router", "switch", "bandwidth", "latency", "dns",
                "ip address", "cannot connect", "unable to connect", "port")) {

            category       = "Network Connectivity Issue";
            assignedRole   = "NETWORK_SUPPORT";
            recommendation = "Check network cables and router status. Restart the network adapter. "
                           + "If VPN-related, reconnect or reinstall the VPN client. "
                           + "Escalate to Network Support if the issue persists.";
            severity = containsAny(text, "entire office", "all users", "everyone", "whole", "organisation",
                                   "all staff", "widespread")
                       ? "HIGH" : "MEDIUM";

        } else if (containsAny(text, "email not", "cannot send", "cannot receive", "email delivery",
                "email failed", "bounce", "email server", "outlook", "smtp", "imap",
                "mail not", "undelivered", "mailbox full", "email blocked")) {

            category       = "Email Delivery Issue";
            assignedRole   = "EMAIL_ADMIN";
            recommendation = "Check mailbox size and spam filters. Verify SMTP/IMAP settings. "
                           + "Contact Email Admin if messages are bouncing or blocked by the mail gateway.";
            severity = containsAny(text, "all users", "everyone", "whole", "entire") ? "HIGH" : "LOW";

        } else if (containsAny(text, "software", "application", "app", "crash", "install",
                "update", "upgrade", "error", "blue screen", "bsod", "not responding",
                "slow", "freeze", "frozen", "restart", "reboot", "driver", "windows",
                "license", "activation", "printer", "hardware", "laptop", "computer",
                "screen", "keyboard", "mouse", "performance", "storage", "disk")) {

            category       = "Desktop / Software Issue";
            assignedRole   = "DESKTOP_SUPPORT";
            recommendation = "Restart the application or device. Check for pending updates. "
                           + "If the issue persists, contact Desktop Support for remote assistance "
                           + "or hardware inspection.";
            severity = containsAny(text, "blue screen", "bsod", "crash", "data loss", "not working at all")
                       ? "HIGH" : "LOW";

        } else {
            // General fallback
            category       = "General Support";
            assignedRole   = "DESKTOP_SUPPORT";
            recommendation = "Log the issue with full details and assign to the appropriate support team. "
                           + "Follow up with the user within one business day.";
            severity       = "LOW";
        }

        // ── SEVERITY OVERRIDE for critical keywords ────────────────────────────
        if (containsAny(text, "critical", "urgent", "emergency", "immediate", "severe",
                "high priority", "all users affected", "production down", "system down")) {
            if (!severity.equals("CRITICAL")) severity = "HIGH";
        }

        Map<String, String> result = new HashMap<>();
        result.put("severity",       severity);
        result.put("category",       category);
        result.put("assignedRole",   assignedRole);
        result.put("recommendation", recommendation);
        return result;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}