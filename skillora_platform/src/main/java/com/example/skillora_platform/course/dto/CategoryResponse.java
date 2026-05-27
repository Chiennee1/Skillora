package com.example.skillora_platform.course.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryResponse {

    private Integer id;
    private String name;
    private String slug;
    private Integer parentId;
    private String parentName;
    private String iconUrl;
    private int orderIndex;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
