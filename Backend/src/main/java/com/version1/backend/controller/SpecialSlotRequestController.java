package com.version1.backend.controller;

import com.version1.backend.dto.SpecialRequestStatusUpdateDto;
import com.version1.backend.dto.SpecialSlotRequestCreateDto;
import com.version1.backend.dto.SpecialSlotRequestResponseDto;
import com.version1.backend.security.UserPrincipal;
import com.version1.backend.service.SpecialSlotRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/special-requests")
public class SpecialSlotRequestController {

    @Autowired
    private SpecialSlotRequestService specialSlotRequestService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SpecialSlotRequestResponseDto> createSpecialRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SpecialSlotRequestCreateDto dto) {
        return new ResponseEntity<>(specialSlotRequestService.createSpecialRequest(principal.getId(), dto), HttpStatus.CREATED);
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<SpecialSlotRequestResponseDto>> getDoctorRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(specialSlotRequestService.getDoctorRequests(principal.getId()));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<SpecialSlotRequestResponseDto>> getCustomerRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(specialSlotRequestService.getCustomerRequests(principal.getId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SpecialSlotRequestResponseDto> updateRequestStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SpecialRequestStatusUpdateDto dto) {
        return ResponseEntity.ok(specialSlotRequestService.updateRequestStatus(principal.getId(), id, dto));
    }
}
