package com.version1.backend.service;

import com.version1.backend.dto.CustomerProfileDto;
import java.util.UUID;

public interface CustomerService {
    CustomerProfileDto getProfileByUserId(UUID userId);
    CustomerProfileDto updateProfile(UUID userId, CustomerProfileDto profileDto);
}
