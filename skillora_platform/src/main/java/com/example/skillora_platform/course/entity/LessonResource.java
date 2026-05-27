package com.example.skillora_platform.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "lesson_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResource extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
