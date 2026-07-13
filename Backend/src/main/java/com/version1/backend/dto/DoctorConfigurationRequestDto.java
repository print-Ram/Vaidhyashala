package com.version1.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorConfigurationRequestDto {
    private DoctorProfileDto profile;
    private List<DoctorAvailabilityDto> availabilities;
}
