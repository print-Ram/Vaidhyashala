package com.version1.backend.controller;

import com.version1.backend.dto.CustomerProfileDto;
import com.version1.backend.security.UserPrincipal;
import com.version1.backend.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileDto> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CustomerProfileDto profile = customerService.getProfileByUserId(userPrincipal.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileDto> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CustomerProfileDto profileDto) {
        CustomerProfileDto updatedProfile = customerService.updateProfile(userPrincipal.getId(), profileDto);
        return ResponseEntity.ok(updatedProfile);
    }
}
