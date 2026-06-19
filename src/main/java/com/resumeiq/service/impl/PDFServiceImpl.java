package com.resumeiq.service.impl;

import com.resumeiq.exception.CustomException;
import com.resumeiq.service.PDFService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Service implementation for PDF operations using Apache PDFBox.
 */
@Service
public class PDFServiceImpl implements PDFService {
    private static final Logger logger = LoggerFactory.getLogger(PDFServiceImpl.class);

    @Override
    public String extractText(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                if (document.isEncrypted()) {
                    throw new CustomException("The PDF file is encrypted and cannot be processed.", HttpStatus.BAD_REQUEST);
                }
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                if (text == null || text.trim().isEmpty()) {
                    throw new CustomException("The PDF file contains no readable text.", HttpStatus.BAD_REQUEST);
                }
                return text;
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to parse PDF document: {}", e.getMessage(), e);
            throw new CustomException("Failed to read PDF file structure: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean verifyIntegrity(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                return !document.isEncrypted() && document.getNumberOfPages() > 0;
            }
        } catch (Exception e) {
            logger.warn("PDF integrity validation failed: {}", e.getMessage());
            return false;
        }
    }
}
