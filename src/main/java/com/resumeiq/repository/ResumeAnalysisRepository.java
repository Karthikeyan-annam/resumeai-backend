package com.resumeiq.repository;

import com.resumeiq.entity.ResumeAnalysis;
import com.resumeiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ResumeAnalysis entities.
 */
@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    Optional<ResumeAnalysis> findByResumeId(Long resumeId);

    @Query("SELECT ra FROM ResumeAnalysis ra JOIN ra.resume r WHERE r.user = :user ORDER BY ra.createdAt DESC")
    List<ResumeAnalysis> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    @Query("SELECT COUNT(ra) FROM ResumeAnalysis ra JOIN ra.resume r WHERE r.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT AVG(ra.atsScore) FROM ResumeAnalysis ra JOIN ra.resume r WHERE r.user = :user")
    Double averageAtsScoreByUser(@Param("user") User user);
}
