package com.example.skillora_platform.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.example.skillora_platform.common.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instructor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "expertise", length = 500)
    private String expertise;

    @Column(name = "intro_video_url", length = 1000)
    private String introVideoUrl;

    @Column(name = "payout_method", length = 50)
    private String payoutMethod;

    @Column(name = "payout_account")
    private String payoutAccount;

    @Column(name = "verified", nullable = false)
    private boolean verified;
}
