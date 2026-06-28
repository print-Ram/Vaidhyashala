package com.version1.backend.service;

import com.version1.backend.dto.CustomerProfileDto;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.pojo.CustomerProfile;
import com.version1.backend.repository.CustomerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileDto getProfileByUserId(UUID userId) {
        CustomerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));
        return mapToDto(profile);
    }

    @Override
    @Transactional
    public CustomerProfileDto updateProfile(UUID userId, CustomerProfileDto dto) {
        CustomerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));

        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setDateOfBirth(dto.getDateOfBirth());

        CustomerProfile updated = profileRepository.save(profile);
        return mapToDto(updated);
    }

    private CustomerProfileDto mapToDto(CustomerProfile profile) {
        return CustomerProfileDto.builder()
                .id(profile.getId())
                .email(profile.getUser().getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .dateOfBirth(profile.getDateOfBirth())
                .build();
    }
}
