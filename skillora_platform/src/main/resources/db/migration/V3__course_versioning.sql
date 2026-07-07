ALTER TABLE courses
    ADD COLUMN current_version INT NULL;

CREATE TABLE course_versions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT | REVIEWING | APPROVED | REJECTED',
    title VARCHAR(255) NULL,
    subtitle VARCHAR(500) NULL,
    description LONGTEXT NULL,
    thumbnail_url VARCHAR(1000) NULL,
    reject_reason VARCHAR(1000) NULL,
    snapshot_json LONGTEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_course_versions PRIMARY KEY (id),
    CONSTRAINT fk_course_versions_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_course_version UNIQUE (course_id, version_number),
    INDEX idx_course_versions_course_status (course_id, status),
    INDEX idx_course_versions_status_updated (status, updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
