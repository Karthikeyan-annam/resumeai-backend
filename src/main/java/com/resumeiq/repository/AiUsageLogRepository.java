package com.resumeiq.repository;

import com.resumeiq.entity.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for AiUsageLog entities.
 */
@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
    List<String> findFeatureUsedByUserId(Long userId);
}
