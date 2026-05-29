package com.example.skillora_platform.enrollment.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LearningDashboardResponse {

    private long totalEnrolled;
    private long inProgress;
    private long completed;
    private long certificatesEarned;
    private List<EnrollmentResponse> recentEnrollments;
}
