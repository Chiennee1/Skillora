package com.example.skillora_platform.enrollment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.enrollment.entity.CourseCertificate;

public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, Long> {

    Optional<CourseCertificate> findByEnrollmentId(Long enrollmentId);

    boolean existsByEnrollmentId(Long enrollmentId);

    Optional<CourseCertificate> findByCertificateCode(String certificateCode);
}
