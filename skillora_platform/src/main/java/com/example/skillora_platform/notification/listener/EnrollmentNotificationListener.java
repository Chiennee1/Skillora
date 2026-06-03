package com.example.skillora_platform.notification.listener;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.skillora_platform.enrollment.entity.CourseCertificate;
import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.repository.CourseCertificateRepository;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.notification.entity.NotificationType;
import com.example.skillora_platform.notification.event.CertificateIssuedEvent;
import com.example.skillora_platform.notification.event.CourseEnrolledEvent;
import com.example.skillora_platform.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnrollmentNotificationListener {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseCertificateRepository courseCertificateRepository;
    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCourseEnrolled(CourseEnrolledEvent event) {
        Enrollment enrollment = enrollmentRepository.findById(event.enrollmentId()).orElse(null);
        if (enrollment == null) {
            return;
        }
        notificationService.createNotification(
                enrollment.getUser().getId(),
                NotificationType.COURSE_ENROLLED,
                "Bạn đã đăng ký khóa học thành công",
                "Khóa " + enrollment.getCourse().getTitle() + " đã được thêm vào học tập của bạn.",
                Map.of(
                        "courseId", enrollment.getCourse().getId(),
                        "enrollmentId", enrollment.getId()
                )
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCertificateIssued(CertificateIssuedEvent event) {
        CourseCertificate certificate = courseCertificateRepository.findById(event.certificateId()).orElse(null);
        if (certificate == null) {
            return;
        }
        Enrollment enrollment = certificate.getEnrollment();
        notificationService.createNotification(
                enrollment.getUser().getId(),
                NotificationType.CERTIFICATE_ISSUED,
                "Bạn đã nhận chứng chỉ",
                "Chúc mừng bạn đã hoàn thành khóa " + enrollment.getCourse().getTitle() + ".",
                Map.of(
                        "courseId", enrollment.getCourse().getId(),
                        "enrollmentId", enrollment.getId(),
                        "certificateId", certificate.getId(),
                        "certificateCode", certificate.getCertificateCode()
                )
        );
    }
}
