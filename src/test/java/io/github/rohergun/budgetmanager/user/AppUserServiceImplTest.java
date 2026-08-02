package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.user.dto.PasswordUpdateRequest;
import io.github.rohergun.budgetmanager.user.dto.UserResponse;
import io.github.rohergun.budgetmanager.user.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private AppUserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    private UUID currentUserId;
    private AppUser existingUser;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();

        existingUser = AppUser.builder()
                .email("john.doe@example.com")
                .name("John")
                .surname("Doe")
                .password("encoded-old-password")
                .build();
    }

    @Test
    void getCurrentUserProfile_returnsMappedResponse_whenUserExists() {
        UserResponse expectedResponse =
                new UserResponse(currentUserId, "john.doe@example.com", "John", "Doe");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(existingUser));
        when(mapper.toResponse(existingUser)).thenReturn(expectedResponse);

        UserResponse result = appUserService.getCurrentUserProfile(currentUserId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).findById(currentUserId);
        verify(mapper).toResponse(existingUser);
    }

    @Test
    void getCurrentUserProfile_throwsUserNotFound_whenUserMissing() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.getCurrentUserProfile(currentUserId))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.USER_NOT_FOUND);

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void updateCurrentUserProfile_updatesNameAndSurname_andReturnsMappedResponse() {
        UserUpdateRequest request = new UserUpdateRequest("Jane", "Smith");
        UserResponse expectedResponse =
                new UserResponse(currentUserId, "john.doe@example.com", "Jane", "Smith");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(existingUser));
        when(mapper.toResponse(existingUser)).thenReturn(expectedResponse);

        UserResponse result = appUserService.updateCurrentUserProfile(currentUserId, request);

        assertThat(existingUser.getName()).isEqualTo("Jane");
        assertThat(existingUser.getSurname()).isEqualTo("Smith");
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateCurrentUserProfile_throwsUserNotFound_whenUserMissing() {
        UserUpdateRequest request = new UserUpdateRequest("Jane", "Smith");
        when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.updateCurrentUserProfile(currentUserId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.USER_NOT_FOUND);
    }

    @Test
    void updateCurrentUserPassword_updatesPassword_whenCurrentPasswordMatches() {
        PasswordUpdateRequest request = new PasswordUpdateRequest("old-raw-password", "new-raw-password");
        UserResponse expectedResponse =
                new UserResponse(currentUserId, "john.doe@example.com", "John", "Doe");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("old-raw-password", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("new-raw-password")).thenReturn("encoded-new-password");
        when(mapper.toResponse(existingUser)).thenReturn(expectedResponse);

        UserResponse result = appUserService.updateCurrentUserPassword(currentUserId, request);

        assertThat(existingUser.getPassword()).isEqualTo("encoded-new-password");
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateCurrentUserPassword_throwsInvalidCredentials_whenCurrentPasswordWrong() {
        PasswordUpdateRequest request = new PasswordUpdateRequest("wrong-password", "new-raw-password");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "encoded-old-password")).thenReturn(false);

        assertThatThrownBy(() -> appUserService.updateCurrentUserPassword(currentUserId, request))
                .isInstanceOf(BudgetManagerException.class)
                .hasFieldOrPropertyWithValue("errorMessage", DomainErrorMessage.INVALID_CREDENTIALS);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deleteCurrentUser_deletesByGivenUserId() {
        appUserService.deleteCurrentUser(currentUserId);

        verify(userRepository).deleteById(currentUserId);
    }
}