package com.resumeiq.service.impl;

import com.resumeiq.exception.CustomException;
import com.resumeiq.service.PDFService;
import com.resumeiq.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Service implementation for file storage using the local file system.
 */
@Service
public class StorageServiceImpl implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    private final Path rootLocation = Paths.get("uploads");
    private final PDFService pdfService;

    public StorageServiceImpl(PDFService pdfService) {
        this.pdfService = pdfService;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            logger.error("Could not initialize storage directory", e);
            throw new CustomException("Failed to initialize storage subsystem", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subdirectory) {
        validateFile(file);

        try {
            Path targetDir = this.rootLocation.resolve(subdirectory);
            Files.createDirectories(targetDir);

            String filename = UUID.randomUUID() + "_" + StringUtilsClean(Objects.requireNonNull(file.getOriginalFilename()));
            Path destinationFile = targetDir.resolve(Paths.get(filename)).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(targetDir.toAbsolutePath())) {
                // Security check against path traversal attack
                throw new CustomException("Cannot store file outside current directory.", HttpStatus.BAD_REQUEST);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.toString();
        } catch (IOException e) {
            logger.error("Failed to store file", e);
            throw new CustomException("Failed to store file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        try {
            Path file = Paths.get(storagePath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.warn("Could not delete file from storage: {}", storagePath, e);
        }
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException("Failed to store empty file.", HttpStatus.BAD_REQUEST);
        }

        // 1. File Size Check (10MB limit)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new CustomException("File size exceeds maximum limit of 10MB.", HttpStatus.BAD_REQUEST);
        }

        // 2. Extension Check
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new CustomException("Only PDF files are supported.", HttpStatus.BAD_REQUEST);
        }

        // 3. Content Type (MIME) Check
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new CustomException("Invalid MIME Type. Only application/pdf is supported.", HttpStatus.BAD_REQUEST);
        }

        // 4. Structural Integrity Check
        try (InputStream is = file.getInputStream()) {
            if (!pdfService.verifyIntegrity(is)) {
                throw new CustomException("PDF structural integrity check failed. File might be corrupted or malicious.", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            throw new CustomException("Failed to validate file integrity.", HttpStatus.BAD_REQUEST);
        }
    }

    private String StringUtilsClean(String name) {
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
