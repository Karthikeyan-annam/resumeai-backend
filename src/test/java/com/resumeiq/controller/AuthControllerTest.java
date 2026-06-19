package com.resumeiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeiq.dto.AuthRequest;
import com.resumeiq.dto.JwtResponse;
import com.resumeiq.dto.RegisterRequest;
import com.resumeiq.entity.User;
import com.resumeiq.security.AuthEntryPointJwt;
import com.resumeiq.security.JwtUtils;
import com.resumeiq.security.UserDetailsServiceImpl;
import com.resumeiq.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests verifying user registration and login endpoints.
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testRegisterUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@resumeiq.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Mockito.when(userService.registerUser(Mockito.any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @WithMockUser
    public void testLoginUser_Success() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@resumeiq.com");
        request.setPassword("password123");

        JwtResponse jwtResponse = new JwtResponse(
                "mockAccessToken",
                "mockRefreshToken",
                1L,
                "testuser",
                "test@resumeiq.com",
                List.of("ROLE_USER")
        );

        Mockito.when(userService.authenticateUser(Mockito.any(AuthRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"))
                .andExpect(jsonPath("$.email").value("test@resumeiq.com"));
    }
}
