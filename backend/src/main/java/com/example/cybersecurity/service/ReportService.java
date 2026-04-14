package com.example.cybersecurity.service;

import com.example.cybersecurity.model.Incident;
import com.example.cybersecurity.repository.IncidentRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final IncidentRepository incidentRepository;

    public ReportService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public byte[] generateIncidentPdfReport() throws Exception {
        List<Incident> incidents = incidentRepository.findAll();

        int total = incidents.size();
        int submitted = 0;
        int assigned = 0;
        int fixing = 0;
        int solved = 0;

        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Integer> severityCounts = new HashMap<>();

        for (Incident incident : incidents) {
            String status = incident.getStatus() == null ? "SUBMITTED" : incident.getStatus().toUpperCase();

            switch (status) {
                case "ASSIGNED" -> assigned++;
                case "FIXING" -> fixing++;
                case "SOLVED" -> solved++;
                default -> submitted++;
            }

            String category = (incident.getCategory() == null || incident.getCategory().isBlank())
                    ? "Uncategorized"
                    : incident.getCategory();

            String severity = (incident.getSeverity() == null || incident.getSeverity().isBlank())
                    ? "UNKNOWN"
                    : incident.getSeverity();

            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            severityCounts.put(severity, severityCounts.getOrDefault(severity, 0) + 1);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        writer.setPageEvent(new PdfFooterEvent());

        document.open();

        Font systemNameFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(37, 99, 235));
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(71, 85, 105));
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(91, 33, 182));
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(30, 41, 59));
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

        addHeader(document, systemNameFont, subtitleFont);
        addTitle(document, titleFont, bodyFont);
        addSummarySection(document, headingFont, total, submitted, assigned, fixing, solved);
        addCategorySection(document, headingFont, categoryCounts);
        addSeveritySection(document, headingFont, severityCounts);

        document.close();
        return outputStream.toByteArray();
    }

    private void addHeader(Document document, Font systemNameFont, Font subtitleFont) throws Exception {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.2f, 4f});
        headerTable.setSpacingAfter(18);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        try {
            ClassPathResource resource = new ClassPathResource("logo.png");
            InputStream inputStream = resource.getInputStream();
            byte[] imageBytes = inputStream.readAllBytes();
            Image logo = Image.getInstance(imageBytes);
            logo.scaleToFit(60, 60);
            logoCell.addElement(logo);
        } catch (Exception e) {
            logoCell.addElement(new Phrase(""));
        }

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph systemName = new Paragraph("Cybersecurity Incident Intelligence Database", systemNameFont);
        systemName.setSpacingAfter(4);

        Paragraph subtitle = new Paragraph("Administrative Security Incident Report", subtitleFont);

        textCell.addElement(systemName);
        textCell.addElement(subtitle);

        headerTable.addCell(logoCell);
        headerTable.addCell(textCell);

        document.add(headerTable);
    }

    private void addTitle(Document document, Font titleFont, Font bodyFont) throws Exception {
        Paragraph title = new Paragraph("IT Security Incident Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        String generatedBy = getCurrentUserEmail();

        Paragraph generated = new Paragraph(
                "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                bodyFont
        );
        generated.setAlignment(Element.ALIGN_CENTER);

        Paragraph byUser = new Paragraph("Generated by: " + generatedBy, bodyFont);
        byUser.setAlignment(Element.ALIGN_CENTER);
        byUser.setSpacingAfter(20);

        document.add(generated);
        document.add(byUser);
    }

    private void addSummarySection(Document document, Font headingFont,
                                   int total, int submitted, int assigned, int fixing, int solved) throws Exception {
        document.add(new Paragraph("Incident Summary", headingFont));
        document.add(new Paragraph(" "));

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setSpacingAfter(20);

        addCell(summaryTable, "Total Incidents", true);
        addCell(summaryTable, String.valueOf(total), false);

        addCell(summaryTable, "Submitted", true);
        addCell(summaryTable, String.valueOf(submitted), false);

        addCell(summaryTable, "Assigned", true);
        addCell(summaryTable, String.valueOf(assigned), false);

        addCell(summaryTable, "Fixing", true);
        addCell(summaryTable, String.valueOf(fixing), false);

        addCell(summaryTable, "Solved", true);
        addCell(summaryTable, String.valueOf(solved), false);

        document.add(summaryTable);
    }

    private void addCategorySection(Document document, Font headingFont,
                                    Map<String, Integer> categoryCounts) throws Exception {
        document.add(new Paragraph("Incidents by Category", headingFont));
        document.add(new Paragraph(" "));

        PdfPTable categoryTable = new PdfPTable(2);
        categoryTable.setWidthPercentage(70);
        categoryTable.setSpacingAfter(20);

        addCell(categoryTable, "Category", true);
        addCell(categoryTable, "Count", true);

        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            addCell(categoryTable, entry.getKey(), false);
            addCell(categoryTable, String.valueOf(entry.getValue()), false);
        }

        document.add(categoryTable);
    }

    private void addSeveritySection(Document document, Font headingFont,
                                    Map<String, Integer> severityCounts) throws Exception {
        document.add(new Paragraph("Incidents by Severity", headingFont));
        document.add(new Paragraph(" "));

        PdfPTable severityTable = new PdfPTable(2);
        severityTable.setWidthPercentage(70);

        addCell(severityTable, "Severity", true);
        addCell(severityTable, "Count", true);

        for (Map.Entry<String, Integer> entry : severityCounts.entrySet()) {
            addCell(severityTable, entry.getKey(), false);
            addCell(severityTable, String.valueOf(entry.getValue()), false);
        }

        document.add(severityTable);
    }

    private void addCell(PdfPTable table, String text, boolean header) {
        Font font = header
                ? new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)
                : new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (header) {
            cell.setBackgroundColor(new BaseColor(79, 70, 229));
        } else {
            cell.setBackgroundColor(BaseColor.WHITE);
        }

        table.addCell(cell);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "Unknown";
        }
        return authentication.getName();
    }

    private static class PdfFooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, new BaseColor(100, 116, 139));
            Phrase footer = new Phrase("Page " + writer.getPageNumber(), footerFont);

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    footer,
                    (document.right() + document.left()) / 2,
                    document.bottom() - 18,
                    0
            );
        }
    }
}