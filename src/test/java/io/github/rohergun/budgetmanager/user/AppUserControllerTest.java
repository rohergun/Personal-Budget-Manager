package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.security.CustomUserDetailsService;
import io.github.rohergun.budgetmanager.security.JwtService;
import io.github.rohergun.budgetmanager.security.LoginRateLimiter;
import io.github.rohergun.budgetmanager.user.dto.PasswordUpdateRequest;
import io.github.rohergun.budgetmanager.user.dto.UserResponse;
import io.github.rohergun.budgetmanager.user.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppUserController.class)
class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private AppUserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private LoginRateLimiter loginRateLimiter;

    private UUID currentUserId;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();

        AppUser user = AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-password")
                .build();

        CustomUserDetails principal = new CustomUserDetails(user) {
            @Override
            public UUID getId() {
                return currentUserId;
            }
        };

        authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }

    @Test
    void getUserProfile_returnsOkWithUserResponse() throws Exception {
        UserResponse response =
                new UserResponse(currentUserId, "john.doe@example.com", "John", "Doe");

        when(userService.getCurrentUserProfile(currentUserId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(currentUserId.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"));

        verify(userService).getCurrentUserProfile(currentUserId);
    }

    @Test
    void updateUserProfile_returnsOkWithUpdatedResponse() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Jane", "Smith");
        UserResponse response =
                new UserResponse(currentUserId, "john.doe@example.com", "Jane", "Smith");

        when(userService.updateCurrentUserProfile(eq(currentUserId), any(UserUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.surname").value("Smith"));

        verify(userService).updateCurrentUserProfile(currentUserId, request);
    }

    @Test
    void updateUserProfile_returnsBadRequest_whenNameBlank() throws Exception {
        UserUpdateRequest invalidRequest = new UserUpdateRequest("", "Smith");

        mockMvc.perform(put("/api/v1/users/me")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateCurrentUserProfile(any(), any());
    }

    @Test
    void updateCurrentUserPassword_returnsOkWithResponse() throws Exception {
        PasswordUpdateRequest request =
                new PasswordUpdateRequest("old-password", "new-password");
        UserResponse response =
                new UserResponse(currentUserId, "john.doe@example.com", "John", "Doe");

        when(userService.updateCurrentUserPassword(eq(currentUserId), any(PasswordUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).updateCurrentUserPassword(currentUserId, request);
    }

    @Test
    void deleteCurrentUser_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteCurrentUser(currentUserId);
    }
}