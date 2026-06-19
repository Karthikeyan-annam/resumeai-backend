package com.resumeiq.service.impl;

import com.resumeiq.dto.*;
import com.resumeiq.entity.*;
import com.resumeiq.exception.CustomException;
import com.resumeiq.exception.TokenRefreshException;
import com.resumeiq.repository.*;
import com.resumeiq.security.JwtUtils;
import com.resumeiq.security.UserDetailsImpl;
import com.resumeiq.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation managing user profile actions and authentications.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserActivityRepository userActivityRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Value("${resumeiq.jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           SubscriptionRepository subscriptionRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           UserActivityRepository userActivityRepository,
                           PasswordEncoder encoder,
                           AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userActivityRepository = userActivityRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email is already in use!", HttpStatus.BAD_REQUEST);
        }

        // Create new user's account
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new CustomException("Error: Role is not found.", HttpStatus.INTERNAL_SERVER_ERROR));
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Provision Free Plan Subscription by default
        Subscription subscription = new Subscription();
        subscription.setUser(savedUser);
        subscription.setPlanType("FREE");
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(LocalDateTime.now());
        subscription.setExpiryDate(LocalDateTime.now().plusYears(10)); // Free plan is active for 10 years
        subscriptionRepository.save(subscription);

        savedUser.setSubscription(subscription);

        // Audit register
        logActivity(savedUser.getEmail(), "REGISTER", "User registered successfully", "0.0.0.0");

        return savedUser;
    }

    @Override
    @Transactional
    public JwtResponse authenticateUser(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Manage and Rotate Refresh Token
        // Delete any existing refresh tokens first (single active session)
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = createRefreshToken(user);

        logActivity(user.getEmail(), "LOGIN", "User logged in successfully", "0.0.0.0");

        return new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Rotate JWT access token
                    String token = jwtUtils.generateTokenFromEmail(user.getEmail());
                    
                    // Rotate Refresh Token (optional but recommended for security)
                    refreshTokenRepository.deleteByUser(user);
                    RefreshToken newRefreshToken = createRefreshToken(user);
                    
                    return new TokenRefreshResponse(token, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @Override
    @Transactional
    public void logoutUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
        logActivity(email, "LOGOUT", "User logged out successfully", "0.0.0.0");
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        String planType = "FREE";
        String status = "ACTIVE";
        if (user.getSubscription() != null) {
            planType = user.getSubscription().getPlanType();
            status = user.getSubscription().getStatus();
        }

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                planType,
                status
        );
    }

    @Override
    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CustomException("Invalid current password.", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        logActivity(email, "PASSWORD_CHANGE", "Password changed successfully", "0.0.0.0");
    }

    @Override
    @Transactional
    public void logActivity(String email, String activityType, String details, String ipAddress) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setActivityType(activityType);
            activity.setDetails(details);
            activity.setIpAddress(ipAddress);
            userActivityRepository.save(activity);
        }
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
