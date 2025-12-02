package com.wms.ems.performance;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    // Create review cycle
    @Transactional
    public PerformanceReview createReview(PerformanceReview review) {
        review.setStatus("Draft");
        return performanceReviewRepository.save(review);
    }

    // Submit review
    @Transactional
    public PerformanceReview submitReview(Long id) {
        PerformanceReview review = performanceReviewRepository.findById(id).orElseThrow();
        review.setStatus("Submitted");
        return performanceReviewRepository.save(review);
    }

    // Acknowledge review
    @Transactional
    public PerformanceReview acknowledgeReview(Long id) {
        PerformanceReview review = performanceReviewRepository.findById(id).orElseThrow();
        review.setStatus("Acknowledged");
        return performanceReviewRepository.save(review);
    }

    public List<PerformanceReview> getReviews(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    // PDF export using iText
    public byte[] exportReviewToPdf(Long id) throws DocumentException {
        PerformanceReview review = performanceReviewRepository.findById(id).orElseThrow();
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Performance Review for Employee: " + review.getEmployeeId()));
        document.add(new Paragraph("Period: " + review.getPeriod()));
        document.add(new Paragraph("Status: " + review.getStatus()));
        document.add(new Paragraph("Goals: " + review.getGoals()));
        document.add(new Paragraph("Competencies: " + review.getCompetencies()));
        document.add(new Paragraph("Ratings: " + review.getRatings()));
        document.add(new Paragraph("Comments: " + review.getComments()));
        document.close();
        return baos.toByteArray();
    }
}
