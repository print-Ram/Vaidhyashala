package com.version1.backend.controller;

import com.version1.backend.dto.LoginRequestDto;
import com.version1.backend.dto.GoogleLoginRequestDto;
import com.version1.backend.dto.TokenResponseDto;
import com.version1.backend.dto.UserRegistrationDto;
import com.version1.backend.dto.TokenRefreshRequestDto;
import com.version1.backend.service.AuthService;
import com.version1.backend.exception.CustomException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
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

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    private void setTokenCookies(HttpServletResponse response, TokenResponseDto tokenResponse) {
        // Access Token Cookie
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(jwtExpirationInMs / 1000)
                .sameSite(cookieSameSite)
                .build();

        // Refresh Token Cookie
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(refreshExpirationInMs / 1000)
                .sameSite(cookieSameSite)
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
    public ResponseEntity<TokenResponseDto> refreshAccessToken(
            @RequestBody(required = false) TokenRefreshRequestDto refreshRequestDto,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        String tokenToUse = null;
        if (refreshRequestDto != null && StringUtils.hasText(refreshRequestDto.getRefreshToken())) {
            tokenToUse = refreshRequestDto.getRefreshToken();
        } else if (StringUtils.hasText(cookieRefreshToken)) {
            tokenToUse = cookieRefreshToken;
        }

        if (tokenToUse == null) {
            throw new CustomException("Refresh token is missing", HttpStatus.BAD_REQUEST);
        }

        TokenResponseDto tokenResponse = authService.refreshAccessToken(tokenToUse);
        setTokenCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.OK.value());
        body.put("message", "Logged out successfully (cookies cleared)");
        return ResponseEntity.ok(body);
    }
}
