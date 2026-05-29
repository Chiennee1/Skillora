package com.example.skillora_platform.enrollment.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CertificateResponse {

    private Long id;
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String studentName;
    private String certificateCode;
    private String pdfUrl;
    private LocalDateTime issuedAt;
}
