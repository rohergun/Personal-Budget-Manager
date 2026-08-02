package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.user.dto.PasswordUpdateRequest;
import io.github.rohergun.budgetmanager.user.dto.UserResponse;
import io.github.rohergun.budgetmanager.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserProfile
            (@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid UserUpdateRequest request){

        return ResponseEntity.ok(userService.updateCurrentUserProfile(principal.getId(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<UserResponse> updateCurrentUserPassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid PasswordUpdateRequest request) {

        return ResponseEntity.ok(userService.updateCurrentUserPassword(principal.getId(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        userService.deleteCurrentUser(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
