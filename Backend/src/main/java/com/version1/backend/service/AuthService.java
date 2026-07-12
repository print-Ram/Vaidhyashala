package com.version1.backend.service;

import com.version1.backend.dto.LoginRequestDto;
import com.version1.backend.dto.TokenResponseDto;
import com.version1.backend.dto.UserRegistrationDto;

public interface AuthService {
    void register(UserRegistrationDto registrationDto);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    TokenResponseDto loginWithGoogle(String email, String name);
    TokenResponseDto refreshAccessToken(String refreshToken);
}
