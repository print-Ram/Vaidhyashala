package com.version1.backend.controller;

import com.version1.backend.dto.LoginRequestDto;
import com.version1.backend.dto.GoogleLoginRequestDto;
import com.version1.backend.dto.TokenResponseDto;
import com.version1.backend.dto.UserRegistrationDto;
import com.version1.backend.dto.TokenRefreshRequestDto;
import com.version1.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${app.jwt.expiration-ms:900000}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationInMs;

    private void setTokenCookies(HttpServletResponse response, TokenResponseDto tokenResponse) {
        // Access Token Cookie
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtExpirationInMs / 1000)
                .sameSite("Lax")
                .build();

        // Refresh Token Cookie
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshExpirationInMs / 1000)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        authService.register(registrationDto);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "User registered successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginDto, HttpServletResponse response) {
        TokenResponseDto tokenResponse = authService.login(loginDto);
        setTokenCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponseDto> authenticateGoogleUser(@Valid @RequestBody GoogleLoginRequestDto googleLoginDto, HttpServletResponse response) {
        TokenResponseDto tokenResponse = authService.loginWithGoogle(googleLoginDto.getEmail(), googleLoginDto.getName());
        setTokenCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshAccessToken(@Valid @RequestBody TokenRefreshRequestDto refreshRequestDto, HttpServletResponse response) {
        TokenResponseDto tokenResponse = authService.refreshAccessToken(refreshRequestDto.getRefreshToken());
        setTokenCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.OK.value());
        body.put("message", "Logged out successfully (cookies cleared)");
        return ResponseEntity.ok(body);
    }
}
