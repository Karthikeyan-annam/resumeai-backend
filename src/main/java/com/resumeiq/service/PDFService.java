package com.resumeiq.service;

import java.io.InputStream;

/**
 * Service interface for processing and extracting text from PDF files.
 */
public interface PDFService {

    /**
     * Extracts raw text from an input stream representing a PDF.
     *
     * @param inputStream the stream of the PDF file
     * @return the extracted text content
     */
    String extractText(InputStream inputStream);

    /**
     * Verifies that the PDF file has integrity (is not corrupted and can be read).
     *
     * @param inputStream the stream of the PDF file
     * @return true if valid, false otherwise
     */
    boolean verifyIntegrity(InputStream inputStream);
}
