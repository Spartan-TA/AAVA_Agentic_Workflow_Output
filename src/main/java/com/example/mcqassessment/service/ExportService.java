package com.example.mcqassessment.service;

import com.example.mcqassessment.domain.AssessmentAttempt;
import com.example.mcqassessment.repository.AssessmentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {
    @Autowired
    private final AssessmentAttemptRepository attemptRepository;

    public byte[] exportResultsToCSV(Long assessmentId) throws IOException {
        List<AssessmentAttempt> attempts = attemptRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out), CSVFormat.DEFAULT.withHeader("AttemptId", "Student", "Score"));
        for (AssessmentAttempt attempt : attempts) {
            if (attempt.getAssessment().getId().equals(assessmentId)) {
                printer.printRecord(attempt.getId(), attempt.getStudentUsername(), attempt.getScore());
            }
        }
        printer.flush();
        return out.toByteArray();
    }

    public byte[] exportResultsToPDF(Long assessmentId) throws IOException {
        List<AssessmentAttempt> attempts = attemptRepository.findAll();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("Assessment Results");
        contentStream.newLineAtOffset(0, -20);
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        for (AssessmentAttempt attempt : attempts) {
            if (attempt.getAssessment().getId().equals(assessmentId)) {
                contentStream.showText("AttemptId: " + attempt.getId() + ", Student: " + attempt.getStudentUsername() + ", Score: " + attempt.getScore());
                contentStream.newLineAtOffset(0, -15);
            }
        }
        contentStream.endText();
        contentStream.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return out.toByteArray();
    }
}
