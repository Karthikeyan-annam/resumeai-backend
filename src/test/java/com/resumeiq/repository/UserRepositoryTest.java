package com.resumeiq.repository;

import com.resumeiq.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data JPA integration test verifying UserRepository logic on H2.
 */
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindByEmail_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@resumeiq.com");
        user.setPassword("secret123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("testuser@resumeiq.com");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    public void testExistsByEmail_ReturnsTrue() {
        User user = new User();
        user.setUsername("existsuser");
        user.setEmail("exists@resumeiq.com");
        user.setPassword("secret123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@resumeiq.com"));
        assertFalse(userRepository.existsByEmail("notfound@resumeiq.com"));
    }
}
