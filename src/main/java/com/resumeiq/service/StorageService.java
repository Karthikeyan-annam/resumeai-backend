package com.resumeiq.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for secure file storage management and verification.
 */
public interface StorageService {

    /**
     * Persists a file in the configured storage engine.
     *
     * @param file the uploaded file
     * @param subdirectory target folder layout inside storage
     * @return the unique storage path or URL referencing the file
     */
    String storeFile(MultipartFile file, String subdirectory);

    /**
     * Deletes a file from storage.
     *
     * @param storagePath the path or URL of the file to delete
     */
    void deleteFile(String storagePath);

    /**
     * Validates file security (extension, MIME type, file size, integrity).
     *
     * @param file the uploaded file
     */
    void validateFile(MultipartFile file);
}
