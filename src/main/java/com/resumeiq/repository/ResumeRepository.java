package com.resumeiq.repository;

import com.resumeiq.entity.Resume;
import com.resumeiq.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Resume entities.
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
    List<Resume> findByUser(User user, Pageable pageable);
    long countByUser(User user);
}
