package com.example.skillora_platform.course.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.skillora_platform.common.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "lessons",
        uniqueConstraints = @UniqueConstraint(name = "uq_lessons_section_order", columnNames = {
                "section_id",
                "order_index"
        })
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug")
    private String slug;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "type", nullable = false, length = 30)
    private LessonType type;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "is_preview", nullable = false)
    private boolean preview;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LessonVideo video;

    @Builder.Default
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonResource> resources = new ArrayList<>();
}
