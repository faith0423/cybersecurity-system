package com.example.cybersecurity.service;

import com.example.cybersecurity.dto.IncidentAnalysisResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class AiAnalysisService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model}")
    private String openAiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String buildRequestBody(String prompt) throws IOException {
        JsonNode body = objectMapper.createObjectNode()
                .put("model", openAiModel)
                .put("input", prompt)
                .put("instructions", "Return only strict JSON.")
                .put("temperature", 0.2);

        return objectMapper.writeValueAsString(body);
    }

    public IncidentAnalysisResponse analyzeIncident(String title, String description) {
        try {
            String safeTitle = title == null ? "" : title.trim();
            String safeDescription = description == null ? "" : description.trim();

            if (safeTitle.isEmpty() && safeDescription.isEmpty()) {
                throw new IllegalArgumentException("Incident title and description are empty.");
            }

            String prompt = buildPrompt(safeTitle, safeDescription);
            String requestBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("X-Client-Request-Id", UUID.randomUUID().toString())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("OpenAI API call failed. Status: "
                        + response.statusCode() + ", Body: " + response.body());
            }

            String modelText = extractOutputText(response.body());
            JsonNode aiJson = objectMapper.readTree(modelText);

            return new IncidentAnalysisResponse(
                    aiJson.path("predictedSeverity").asText("MEDIUM"),
                    aiJson.path("predictedCategory").asText("Other"),
                    aiJson.path("recommendedRole").asText("DESKTOP_SUPPORT"),
                    aiJson.path("recommendation").asText("Manual review required."),
                    true,
                    null
            );

        } catch (Exception e) {
            return new IncidentAnalysisResponse(
                    "MEDIUM",
                    "Other",
                    "DESKTOP_SUPPORT",
                    "AI analysis failed. Please review the incident manually.",
                    false,
                    e.getMessage()
            );
        }
    }

    private String buildPrompt(String title, String description) throws IOException {
        String escapedTitle = objectMapper.writeValueAsString(title);
        String escapedDescription = objectMapper.writeValueAsString(description);

        return """
                You are a cybersecurity incident triage assistant.

                Carefully analyze the incident title and description and classify it.

                IMPORTANT:
                - Do NOT guess phishing unless clearly email-related.
                - Identify the TRUE nature of the attack.
                - Use context (network, login, system, traffic, malware, etc).

                Return ONLY valid JSON with NO extra text.

                JSON format:
                {
                  "predictedSeverity": "LOW | MEDIUM | HIGH | CRITICAL",
                  "predictedCategory": "Phishing | Malware | DDoS | Unauthorized Access | Data Breach | Insider Threat | Other",
                  "recommendedRole": "SECURITY_ANALYST | DESKTOP_SUPPORT | NETWORK_ADMIN | INCIDENT_RESPONDER",
                  "recommendation": "short actionable response"
                }

                Incident title:
                """ + escapedTitle + """

                Incident description:
                """ + escapedDescription;
    }

    private String extractOutputText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (content.isArray()) {
                    for (JsonNode part : content) {
                        if ("output_text".equals(part.path("type").asText())) {
                            return part.path("text").asText();
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Could not extract model text from OpenAI response.");
    }
}