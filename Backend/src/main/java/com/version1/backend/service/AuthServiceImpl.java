package com.version1.backend.service;

import com.version1.backend.dto.LoginRequestDto;
import com.version1.backend.dto.TokenResponseDto;
import com.version1.backend.dto.UserRegistrationDto;
import com.version1.backend.exception.CustomException;
import com.version1.backend.pojo.*;
import com.version1.backend.enums.Role;
import com.version1.backend.enums.UserStatus;
import com.version1.backend.repository.AddressRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.repository.UserRepository;
import com.version1.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public void register(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("Email is already in use", HttpStatus.BAD_REQUEST);
        }

        // 1. Create and save User
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        // 2. Create and save CustomerProfile
        CustomerProfile profile = CustomerProfile.builder()
                .user(savedUser)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .build();
        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        // 3. Create and save Address
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

    @Override
    @Transactional
    public TokenResponseDto login(LoginRequestDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = UUID.randomUUID().toString();

        return TokenResponseDto.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .build();
    }

    @Override
    @Transactional
    public TokenResponseDto loginWithGoogle(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new CustomException("User not registered", HttpStatus.NOT_FOUND);
        }

        String jwt = tokenProvider.generateTokenFromEmail(email);
        String refreshToken = UUID.randomUUID().toString();

        return TokenResponseDto.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .build();
    }
}
