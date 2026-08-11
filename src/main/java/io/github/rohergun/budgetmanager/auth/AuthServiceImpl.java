package io.github.rohergun.budgetmanager.auth;

import io.github.rohergun.budgetmanager.auth.dto.AuthResponse;
import io.github.rohergun.budgetmanager.auth.dto.LoginRequest;
import io.github.rohergun.budgetmanager.auth.dto.RegisterRequest;
import io.github.rohergun.budgetmanager.exception.BudgetManagerException;
import io.github.rohergun.budgetmanager.exception.DomainErrorMessage;
import io.github.rohergun.budgetmanager.security.CustomUserDetails;
import io.github.rohergun.budgetmanager.security.JwtService;
import io.github.rohergun.budgetmanager.user.AppUser;
import io.github.rohergun.budgetmanager.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.email())) {
            throw new BudgetManagerException(DomainErrorMessage.EMAIL_ALREADY_EXISTS);
        }
        AppUser newUser = AppUser.builder()
                .email(request.email())
                .name(request.name())
                .surname(request.surname())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(newUser);
        return new AuthResponse(jwtService.generateToken(new CustomUserDetails(newUser)));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BudgetManagerException(DomainErrorMessage.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(new CustomUserDetails(user)));
    }
}
