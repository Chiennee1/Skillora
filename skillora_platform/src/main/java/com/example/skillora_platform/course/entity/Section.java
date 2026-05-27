package com.example.skillora_platform.course.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.example.skillora_platform.common.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "sections",
        uniqueConstraints = @UniqueConstraint(name = "uq_sections_course_order", columnNames = {
                "course_id",
                "order_index"
        })
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();
}
