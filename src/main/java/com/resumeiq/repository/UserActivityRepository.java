package com.resumeiq.repository;

import com.resumeiq.entity.User;
import com.resumeiq.entity.UserActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserActivity entities.
 */
@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserOrderByCreatedAtDesc(User user);
    List<UserActivity> findByUser(User user, Pageable pageable);
}
