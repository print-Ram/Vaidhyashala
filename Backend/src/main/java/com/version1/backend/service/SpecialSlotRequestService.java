package com.version1.backend.service;

import com.version1.backend.dto.SpecialRequestStatusUpdateDto;
import com.version1.backend.dto.SpecialSlotRequestCreateDto;
import com.version1.backend.dto.SpecialSlotRequestResponseDto;

import java.util.List;
import java.util.UUID;

public interface SpecialSlotRequestService {
    SpecialSlotRequestResponseDto createSpecialRequest(UUID customerUserId, SpecialSlotRequestCreateDto dto);
    List<SpecialSlotRequestResponseDto> getDoctorRequests(UUID doctorUserId);
    List<SpecialSlotRequestResponseDto> getCustomerRequests(UUID customerUserId);
    SpecialSlotRequestResponseDto updateRequestStatus(UUID doctorUserId, UUID requestId, SpecialRequestStatusUpdateDto dto);
}
