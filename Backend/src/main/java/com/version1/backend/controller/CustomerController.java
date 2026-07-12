package com.version1.backend.controller;

import com.version1.backend.dto.AddressDto;
import com.version1.backend.dto.CustomerProfileDto;
import com.version1.backend.exception.CustomException;
import com.version1.backend.exception.ResourceNotFoundException;
import com.version1.backend.pojo.Address;
import com.version1.backend.pojo.CustomerProfile;
import com.version1.backend.repository.AddressRepository;
import com.version1.backend.repository.CustomerProfileRepository;
import com.version1.backend.security.UserPrincipal;
import com.version1.backend.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired private CustomerService customerService;
    @Autowired private AddressRepository addressRepository;
    @Autowired private CustomerProfileRepository customerProfileRepository;

    // -----------------------------------------------------------------------
    // Profile
    // -----------------------------------------------------------------------

    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileDto> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(customerService.getProfileByUserId(userPrincipal.getId()));
    }

    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileDto> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CustomerProfileDto profileDto) {
        return ResponseEntity.ok(customerService.updateProfile(userPrincipal.getId(), profileDto));
    }

    // -----------------------------------------------------------------------
    // Address (deferred at signup, manageable later via profile section)
    // -----------------------------------------------------------------------

    /**
     * GET /api/v1/customers/me/address
     * Returns all saved addresses for the authenticated customer.
     */
    @GetMapping("/me/address")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AddressDto>> getMyAddresses(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        CustomerProfile profile = findProfile(userPrincipal.getId());
        List<AddressDto> addresses = addressRepository.findByProfileId(profile.getId())
                .stream().map(this::toAddressDto).collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }

    /**
     * POST /api/v1/customers/me/address
     * Creates a new address for the customer (e.g. after skipping it at signup).
     */
    @PostMapping("/me/address")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AddressDto> createAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody AddressDto dto) {
        CustomerProfile profile = findProfile(userPrincipal.getId());
        Address address = Address.builder()
                .profile(profile)
                .streetAddress(dto.getStreetAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .isPrimary(dto.isPrimary())
                .build();
        return new ResponseEntity<>(toAddressDto(addressRepository.save(address)), HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/customers/me/address/{id}
     * Updates an existing address.
     */
    @PutMapping("/me/address/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AddressDto> updateAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @RequestBody AddressDto dto) {
        CustomerProfile profile = findProfile(userPrincipal.getId());
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));

        // Ensure the address belongs to this customer
        if (!address.getProfile().getId().equals(profile.getId())) {
            throw new CustomException("Access denied: address does not belong to this customer", HttpStatus.FORBIDDEN);
        }

        if (dto.getStreetAddress() != null) address.setStreetAddress(dto.getStreetAddress());
        if (dto.getCity() != null)          address.setCity(dto.getCity());
        if (dto.getState() != null)         address.setState(dto.getState());
        if (dto.getPostalCode() != null)    address.setPostalCode(dto.getPostalCode());
        if (dto.getCountry() != null)       address.setCountry(dto.getCountry());
        address.setPrimary(dto.isPrimary());

        return ResponseEntity.ok(toAddressDto(addressRepository.save(address)));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CustomerProfile findProfile(UUID userId) {
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + userId));
    }

    private AddressDto toAddressDto(Address a) {
        return AddressDto.builder()
                .id(a.getId())
                .streetAddress(a.getStreetAddress())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .isPrimary(a.isPrimary())
                .build();
    }
}

