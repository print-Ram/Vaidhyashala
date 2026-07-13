package com.version1.backend.repository;

import com.version1.backend.pojo.SpecialSlotRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialSlotRequestRepository extends JpaRepository<SpecialSlotRequest, UUID> {

    @Query("SELECT s FROM SpecialSlotRequest s WHERE s.doctor.user.id = :doctorUserId")
    List<SpecialSlotRequest> findByDoctorUserId(@Param("doctorUserId") UUID doctorUserId);

    @Query("SELECT s FROM SpecialSlotRequest s WHERE s.customer.user.id = :customerUserId")
    List<SpecialSlotRequest> findByCustomerUserId(@Param("customerUserId") UUID customerUserId);
}
