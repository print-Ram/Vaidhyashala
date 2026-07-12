package com.version1.backend.service;

import com.version1.backend.dto.LoginRequestDto;
import com.version1.backend.dto.TokenResponseDto;
import com.version1.backend.dto.UserRegistrationDto;
import com.version1.backend.exception.CustomException;
import com.version1.backend.pojo.*;
import com.version1.backend.enums.Role;
import com.version1.backend.enums.UserStatus;
import com.version1.backend.enums.AdminNotificationType;
import com.version1.backend.enums.DoctorStatus;
import com.version1.backend.repository.AddressRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.repository.UserRepository;
import com.version1.backend.repository.RefreshTokenRepository;
import com.version1.backend.repository.DoctorProfileRepository;
import com.version1.backend.repository.AdminNotificationRepository;
import com.version1.backend.security.JwtTokenProvider;
import com.version1.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private AdminNotificationRepository adminNotificationRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public void register(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("Email is already in use", HttpStatus.BAD_REQUEST);
        }

        Role targetRole = dto.getRole() != null ? dto.getRole() : Role.CUSTOMER;

        // 1. Create and save User
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(targetRole)
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        // 2. Profile Creation based on Role
        if (targetRole == Role.CUSTOMER) {
            CustomerProfile profile = CustomerProfile.builder()
                    .user(savedUser)
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .phoneNumber(dto.getPhoneNumber())
                    .dateOfBirth(dto.getDateOfBirth())
                    .build();
            CustomerProfile savedProfile = customerProfileRepository.save(profile);

            // Create and save Address only if at least one address field was provided.
            boolean hasAddress = dto.getStreetAddress() != null && !dto.getStreetAddress().isBlank()
                    || dto.getCity() != null && !dto.getCity().isBlank()
                    || dto.getState() != null && !dto.getState().isBlank()
                    || dto.getPostalCode() != null && !dto.getPostalCode().isBlank()
                    || dto.getCountry() != null && !dto.getCountry().isBlank();

            if (hasAddress) {
                Address address = Address.builder()
                        .profile(savedProfile)
                        .streetAddress(dto.getStreetAddress())
                        .city(dto.getCity())
                        .state(dto.getState())
                        .postalCode(dto.getPostalCode())
                        .country(dto.getCountry())
                        .isPrimary(true)
                        .build();
                addressRepository.save(address);
            }
        } else if (targetRole == Role.DOCTOR) {
            DoctorProfile profile = DoctorProfile.builder()
                    .user(savedUser)
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .phoneNumber(dto.getPhoneNumber())
                    .status(DoctorStatus.PENDING_APPROVAL)
                    .build();
            DoctorProfile savedProfile = doctorProfileRepository.save(profile);

            // Create admin notification
            String message = String.format(
                    "New doctor registered and awaiting approval. Name: %s %s | Email: %s",
                    savedProfile.getFirstName(), savedProfile.getLastName(),
                    savedUser.getEmail()
            );
            AdminNotification notification = AdminNotification.builder()
                    .type(AdminNotificationType.DOCTOR_REGISTRATION)
                    .referenceDoctorId(savedProfile.getId())
                    .message(message)
                    .build();
            adminNotificationRepository.save(notification);

            // Send email notification to ADMIN / PROVIDER
            emailService.sendAdminDoctorRegistrationNotification(
                    savedProfile.getFirstName() + " " + savedProfile.getLastName(),
                    savedUser.getEmail(),
                    "",
                    ""
            );
        }
    }

    private String createAndSaveRefreshToken(User user) {
        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .user(user)
                .expiryDate(LocalDateTime.now().plus(refreshExpirationMs, java.time.temporal.ChronoUnit.MILLIS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenString;
    }

    @Override
    @Transactional
    public TokenResponseDto login(LoginRequestDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        String refreshToken = createAndSaveRefreshToken(user);

        return TokenResponseDto.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .build();
    }

    @Override
    @Transactional
    public TokenResponseDto loginWithGoogle(String email, String name) {
        User user;
        if (!userRepository.existsByEmail(email)) {
            user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            user = userRepository.save(user);

            CustomerProfile profile = CustomerProfile.builder()
                    .user(user)
                    .firstName(name != null && !name.isBlank() ? name : "Google User")
                    .build();
            customerProfileRepository.save(profile);
        } else {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

            CustomerProfile profile = customerProfileRepository.findByUserId(user.getId())
                    .orElse(null);
            if (profile == null) {
                profile = CustomerProfile.builder()
                        .user(user)
                        .firstName(name != null && !name.isBlank() ? name : "Google User")
                        .build();
                customerProfileRepository.save(profile);
            } else if (name != null && !name.isBlank()) {
                profile.setFirstName(name);
                customerProfileRepository.save(profile);
            }
        }

        String jwt = tokenProvider.generateTokenFromEmail(email);
        String refreshToken = createAndSaveRefreshToken(user);

        return TokenResponseDto.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .build();
    }

    @Override
    @Transactional
    public TokenResponseDto refreshAccessToken(String refreshTokenString) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (oldRefreshToken.isRevoked()) {
            throw new CustomException("Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
        }

        if (oldRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            oldRefreshToken.setRevoked(true);
            refreshTokenRepository.save(oldRefreshToken);
            throw new CustomException("Refresh token has expired", HttpStatus.UNAUTHORIZED);
        }

        User user = oldRefreshToken.getUser();

        String newJwt = tokenProvider.generateTokenFromEmail(user.getEmail());

        return TokenResponseDto.builder()
                .accessToken(newJwt)
                .refreshToken(refreshTokenString)
                .expiresIn(900)
                .build();
    }
}
