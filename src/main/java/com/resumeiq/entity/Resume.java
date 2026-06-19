package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an uploaded Resume file and its metadata.
 */
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
public class Resume extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Lob
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
