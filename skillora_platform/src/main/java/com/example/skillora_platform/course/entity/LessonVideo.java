package com.example.skillora_platform.course.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.skillora_platform.common.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lesson_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonVideo extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false, unique = true)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "provider", nullable = false, length = 50)
    private VideoProvider provider;

    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "original_file_url", length = 1000)
    private String originalFileUrl;

    @Column(name = "playback_url", length = 1000)
    private String playbackUrl;

    @Column(name = "hls_url", length = 1000)
    private String hlsUrl;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 30)
    private VideoStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Builder.Default
    @OneToMany(mappedBy = "lessonVideo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonVideoVariant> variants = new ArrayList<>();
}
