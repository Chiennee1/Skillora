/*
  Default demo password for all accounts: Password@123
  Note: password_hash below is a bcrypt demo hash. Replace in production.
*/

USE skillora;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE audit_logs;
TRUNCATE TABLE course_stats;
TRUNCATE TABLE notifications;
TRUNCATE TABLE chat_messages;
TRUNCATE TABLE chat_conversations;
TRUNCATE TABLE review_likes;
TRUNCATE TABLE reviews;
TRUNCATE TABLE assignment_submissions;
TRUNCATE TABLE assignments;
TRUNCATE TABLE quiz_attempt_answer_options;
TRUNCATE TABLE quiz_attempt_answers;
TRUNCATE TABLE quiz_attempts;
TRUNCATE TABLE answer_options;
TRUNCATE TABLE questions;
TRUNCATE TABLE quizzes;
TRUNCATE TABLE course_certificates;
TRUNCATE TABLE lesson_progress;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE payment_transactions;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE coupons;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE carts;
TRUNCATE TABLE wishlists;
TRUNCATE TABLE lesson_resources;
TRUNCATE TABLE lesson_video_variants;
TRUNCATE TABLE lesson_videos;
TRUNCATE TABLE lessons;
TRUNCATE TABLE sections;
TRUNCATE TABLE course_outcomes;
TRUNCATE TABLE course_requirements;
TRUNCATE TABLE course_categories;
TRUNCATE TABLE courses;
TRUNCATE TABLE categories;
TRUNCATE TABLE password_reset_tokens;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE instructor_profiles;
TRUNCATE TABLE user_profiles;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE roles;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

START TRANSACTION;

-- 01. ROLES / USERS / AUTH

INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'System administrator with full platform permissions'),
(2, 'INSTRUCTOR', 'Instructor who can create and manage courses'),
(3, 'STUDENT', 'Student who can enroll and learn courses');

INSERT INTO users (id, email, password_hash, full_name, avatar_url, status, email_verified, last_login_at, created_at) VALUES
(1, 'admin@skillora.vn', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Admin Skillora', 'https://api.dicebear.com/7.x/avataaars/svg?seed=skillora-admin', 'ACTIVE', TRUE, '2026-05-20 08:15:00', '2026-01-01 08:00:00'),
(2, 'nguyen.van.hung@skillora.vn', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Nguyễn Văn Hùng', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hung', 'ACTIVE', TRUE, '2026-05-21 09:00:00', '2026-01-02 08:00:00'),
(3, 'tran.thi.lan@skillora.vn', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Trần Thị Lan', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lan', 'ACTIVE', TRUE, '2026-05-19 10:20:00', '2026-01-02 08:10:00'),
(4, 'le.minh.duc@skillora.vn', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Lê Minh Đức', 'https://api.dicebear.com/7.x/avataaars/svg?seed=duc', 'ACTIVE', TRUE, '2026-05-18 11:30:00', '2026-01-02 08:20:00'),
(5, 'student01@gmail.com', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Hoàng Văn An', 'https://api.dicebear.com/7.x/avataaars/svg?seed=an', 'ACTIVE', TRUE, '2026-05-22 07:50:00', '2026-01-05 08:00:00'),
(6, 'student02@gmail.com', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Lý Thị Bình', 'https://api.dicebear.com/7.x/avataaars/svg?seed=binh', 'ACTIVE', TRUE, '2026-05-20 20:05:00', '2026-01-05 08:10:00'),
(7, 'student03@gmail.com', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Đặng Văn Cường', 'https://api.dicebear.com/7.x/avataaars/svg?seed=cuong', 'ACTIVE', TRUE, '2026-05-17 12:45:00', '2026-01-05 08:20:00'),
(8, 'student04@gmail.com', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'Bùi Thị Dung', 'https://api.dicebear.com/7.x/avataaars/svg?seed=dung', 'ACTIVE', TRUE, NULL, '2026-01-05 08:30:00'),
(9, 'banned@skillora.vn', '$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC', 'User Bị Khóa', NULL, 'BANNED', FALSE, NULL, '2026-01-05 08:40:00');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2), (3, 2), (4, 2),
(5, 3), (6, 3), (7, 3), (8, 3), (9, 3);

INSERT INTO user_profiles (id, user_id, phone, headline, bio, website, location) VALUES
(1, 1, '0900000001', 'Skillora Platform Admin', 'Quản trị hệ thống Skillora.', NULL, 'Hà Nội'),
(2, 2, '0901234567', 'Senior Backend Developer | Java & Spring Boot', 'Kỹ sư phần mềm với hơn 8 năm kinh nghiệm xây dựng hệ thống backend, REST API, microservices và cloud deployment.', 'https://github.com/nguyenvanhung', 'Hà Nội'),
(3, 3, '0912345678', 'Frontend Developer | ReactJS & UI/UX', 'Frontend developer yêu thích React, TypeScript, design system và trải nghiệm người dùng.', 'https://tranthilan.dev', 'TP.HCM'),
(4, 4, '0923456789', 'Data Scientist | Machine Learning Engineer', 'Chuyên gia dữ liệu với kinh nghiệm Python, Machine Learning, SQL và Data Analytics.', NULL, 'Đà Nẵng'),
(5, 5, '0945678901', 'Sinh viên CNTT | Đam mê Backend', 'Đang học lập trình backend và muốn xây dựng portfolio thực tế.', NULL, 'Hà Nội'),
(6, 6, '0956789012', 'Fresher Frontend Developer', 'Đang học React và TypeScript để ứng tuyển frontend developer.', NULL, 'TP.HCM'),
(7, 7, '0967890123', 'Backend Developer', 'Muốn nâng cao kỹ năng NestJS, SQL và kiến trúc hệ thống.', NULL, 'Hà Nội'),
(8, 8, '0978901234', 'UI/UX Learner', 'Đang học thiết kế giao diện và xây dựng sản phẩm số.', NULL, 'Cần Thơ');

INSERT INTO instructor_profiles (id, user_id, title, expertise, intro_video_url, payout_method, payout_account, verified) VALUES
(1, 2, 'Senior Backend Instructor', 'Java, Spring Boot, Microservices, Database Design', 'https://cdn.skillora.vn/instructors/hung/intro.m3u8', 'BANK_TRANSFER', 'VCB-0123456789', TRUE),
(2, 3, 'Frontend & UI/UX Instructor', 'ReactJS, TypeScript, Tailwind CSS, UX Design', 'https://cdn.skillora.vn/instructors/lan/intro.m3u8', 'BANK_TRANSFER', 'TCB-0987654321', TRUE),
(3, 4, 'Data & AI Instructor', 'Python, SQL, Data Science, Machine Learning', 'https://cdn.skillora.vn/instructors/duc/intro.m3u8', 'BANK_TRANSFER', 'MB-1122334455', TRUE);

INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, created_by_ip, user_agent, created_at) VALUES
(1, 5, 'sha256_demo_refresh_token_hash_student01_202605', '2026-06-22 00:00:00', '127.0.0.1', 'Chrome Windows', '2026-05-22 07:50:00'),
(2, 1, 'sha256_demo_refresh_token_hash_admin_202605', '2026-06-20 00:00:00', '127.0.0.1', 'Chrome Windows', '2026-05-20 08:15:00');

INSERT INTO password_reset_tokens (id, user_id, token_hash, expires_at, used_at, created_at) VALUES
(1, 8, 'sha256_demo_password_reset_hash_student04', '2026-05-23 12:00:00', NULL, '2026-05-22 12:00:00');


-- 02. CATEGORIES / COURSES

INSERT INTO categories (id, name, slug, parent_id, icon_url, order_index, is_active) VALUES
(1, 'Lập trình', 'lap-trinh', NULL, 'https://cdn.skillora.vn/icons/code.svg', 1, TRUE),
(2, 'Thiết kế', 'thiet-ke', NULL, 'https://cdn.skillora.vn/icons/design.svg', 2, TRUE),
(3, 'Dữ liệu & AI', 'du-lieu-ai', NULL, 'https://cdn.skillora.vn/icons/data-ai.svg', 3, TRUE),
(4, 'Marketing', 'marketing', NULL, 'https://cdn.skillora.vn/icons/marketing.svg', 4, TRUE),
(5, 'Backend', 'backend', 1, NULL, 1, TRUE),
(6, 'Frontend', 'frontend', 1, NULL, 2, TRUE),
(7, 'Database', 'database', 1, NULL, 3, TRUE),
(8, 'UI/UX Design', 'ui-ux-design', 2, NULL, 1, TRUE),
(9, 'Figma', 'figma', 2, NULL, 2, TRUE),
(10, 'Machine Learning', 'machine-learning', 3, NULL, 1, TRUE),
(11, 'Python cho Data', 'python-cho-data', 3, NULL, 2, TRUE),
(12, 'SEO & Content', 'seo-content', 4, NULL, 1, TRUE);

INSERT INTO courses (
    id, instructor_id, title, slug, subtitle, description, thumbnail_url, preview_video_url,
    level, language, price, discount_price, currency, status, total_lessons, total_duration_seconds,
    total_enrollments, avg_rating, total_reviews, published_at, created_at
) VALUES
(1, 2, 'Java Spring Boot từ Zero đến Hero', 'java-spring-boot-zero-to-hero', 'Xây dựng REST API, JWT, JPA, Redis và deploy thực tế', 'Khóa học Java Spring Boot toàn diện từ cơ bản đến nâng cao. Học viên sẽ xây dựng một REST API hoàn chỉnh, tích hợp authentication, database, cache và deployment.', 'https://picsum.photos/seed/skillora-java-spring/1280/720', 'https://cdn.skillora.vn/courses/java-spring/preview/master.m3u8', 'BEGINNER', 'vi', 299000.00, 199000.00, 'VND', 'PUBLISHED', 6, 6300, 2, 4.50, 2, '2026-02-01 08:00:00', '2026-01-20 08:00:00'),
(2, 2, 'Microservices với Spring Cloud', 'microservices-spring-cloud', 'Thiết kế hệ thống phân tán với Gateway, Eureka và Resilience', 'Khóa học dành cho developer đã biết Spring Boot và muốn chuyển sang kiến trúc microservices thực chiến.', 'https://picsum.photos/seed/skillora-microservices/1280/720', 'https://cdn.skillora.vn/courses/microservices/preview/master.m3u8', 'ADVANCED', 'vi', 499000.00, 399000.00, 'VND', 'PUBLISHED', 5, 6900, 1, 5.00, 1, '2026-02-10 08:00:00', '2026-01-25 08:00:00'),
(3, 3, 'ReactJS & TypeScript Web Hiện Đại', 'reactjs-typescript-web-hien-dai', 'Xây dựng frontend hiện đại với React, TypeScript, Tailwind và API', 'Khóa học ReactJS kết hợp TypeScript, tập trung vào component architecture, state management, API integration và deployment.', 'https://picsum.photos/seed/skillora-react/1280/720', 'https://cdn.skillora.vn/courses/react-ts/preview/master.m3u8', 'INTERMEDIATE', 'vi', 349000.00, 249000.00, 'VND', 'PUBLISHED', 6, 5400, 2, 4.50, 2, '2026-02-15 08:00:00', '2026-01-28 08:00:00'),
(4, 4, 'Machine Learning với Python Thực Chiến', 'machine-learning-python-thuc-chien', 'Từ pandas, scikit-learn đến mô hình dự đoán thực tế', 'Khóa học Machine Learning dành cho người đã biết Python cơ bản, đi từ xử lý dữ liệu đến xây dựng model và đánh giá kết quả.', 'https://picsum.photos/seed/skillora-ml-python/1280/720', 'https://cdn.skillora.vn/courses/ml-python/preview/master.m3u8', 'INTERMEDIATE', 'vi', 449000.00, 349000.00, 'VND', 'PUBLISHED', 6, 7200, 1, 4.00, 1, '2026-03-01 08:00:00', '2026-02-10 08:00:00'),
(5, 3, 'UI/UX Design với Figma cho Developer', 'ui-ux-design-figma-cho-developer', 'Thiết kế giao diện rõ ràng, đẹp và dễ code hơn', 'Khóa học giúp developer hiểu UI/UX, layout, spacing, typography, design system và cách dùng Figma để thiết kế sản phẩm.', 'https://picsum.photos/seed/skillora-uiux-figma/1280/720', 'https://cdn.skillora.vn/courses/uiux-figma/preview/master.m3u8', 'BEGINNER', 'vi', 259000.00, 159000.00, 'VND', 'PUBLISHED', 5, 4800, 1, 5.00, 1, '2026-03-05 08:00:00', '2026-02-15 08:00:00'),
(6, 2, 'Database Design cho Web App', 'database-design-cho-web-app', 'Thiết kế database chuẩn, tối ưu index, transaction và migration', 'Khóa học thực chiến về thiết kế database cho web app: chuẩn hóa, khóa ngoại, index, transaction, seed data và migration.', 'https://picsum.photos/seed/skillora-db-design/1280/720', 'https://cdn.skillora.vn/courses/db-design/preview/master.m3u8', 'ALL_LEVELS', 'vi', 399000.00, NULL, 'VND', 'REVIEWING', 4, 4200, 0, 0.00, 0, NULL, '2026-04-01 08:00:00');

INSERT INTO course_categories (course_id, category_id) VALUES
(1, 1), (1, 5),
(2, 1), (2, 5),
(3, 1), (3, 6),
(4, 3), (4, 10), (4, 11),
(5, 2), (5, 8), (5, 9),
(6, 1), (6, 7);

INSERT INTO course_requirements (id, course_id, description, order_index) VALUES
(1, 1, 'Biết cú pháp Java cơ bản là lợi thế.', 1),
(2, 1, 'Máy tính đã cài JDK 17 hoặc mới hơn.', 2),
(3, 2, 'Đã biết Spring Boot và REST API cơ bản.', 1),
(4, 3, 'Biết HTML, CSS và JavaScript cơ bản.', 1),
(5, 4, 'Biết Python cơ bản và thao tác với file CSV.', 1),
(6, 5, 'Không cần kinh nghiệm thiết kế trước đó.', 1),
(7, 6, 'Có kiến thức SQL cơ bản là lợi thế.', 1);

INSERT INTO course_outcomes (id, course_id, description, order_index) VALUES
(1, 1, 'Xây dựng REST API hoàn chỉnh với Spring Boot.', 1),
(2, 1, 'Tích hợp JWT authentication và phân quyền.', 2),
(3, 1, 'Làm việc với JPA, migration, validation và cache.', 3),
(4, 2, 'Thiết kế hệ thống microservices có API Gateway.', 1),
(5, 3, 'Xây dựng SPA bằng ReactJS và TypeScript.', 1),
(6, 3, 'Tổ chức component, hook, API layer và state management.', 2),
(7, 4, 'Xây dựng model Machine Learning với scikit-learn.', 1),
(8, 5, 'Thiết kế màn hình web/app bằng Figma.', 1),
(9, 6, 'Thiết kế schema database dễ mở rộng cho web app.', 1);


-- 03. SECTIONS / LESSONS / VIDEOS / RESOURCES

INSERT INTO sections (id, course_id, title, description, order_index, is_published) VALUES
(1, 1, 'Nền tảng Spring Boot', 'Thiết lập môi trường và xây dựng API đầu tiên.', 1, TRUE),
(2, 1, 'REST API & Authentication', 'Xây dựng API, JWT và security.', 2, TRUE),
(3, 2, 'Microservices Foundation', 'Tư duy thiết kế hệ thống phân tán.', 1, TRUE),
(4, 2, 'Gateway & Service Discovery', 'API Gateway, discovery và resilience.', 2, TRUE),
(5, 3, 'React TypeScript Foundation', 'Thiết lập dự án và component cơ bản.', 1, TRUE),
(6, 3, 'Project Structure & API', 'Tổ chức code frontend thực chiến.', 2, TRUE),
(7, 4, 'Data Preparation', 'Làm sạch và chuẩn bị dữ liệu.', 1, TRUE),
(8, 4, 'Training Models', 'Huấn luyện và đánh giá mô hình.', 2, TRUE),
(9, 5, 'UI/UX Foundation', 'Nguyên lý thiết kế giao diện.', 1, TRUE),
(10, 5, 'Figma Workflow', 'Thiết kế màn hình và design system.', 2, TRUE),
(11, 6, 'Database Foundation', 'Từ requirement đến ERD.', 1, TRUE),
(12, 6, 'Performance & Migration', 'Index, seed data, migration và transaction.', 2, TRUE);

INSERT INTO lessons (id, section_id, title, slug, type, content, duration_seconds, is_preview, is_published, order_index) VALUES
(1, 1, 'Giới thiệu khóa học Spring Boot', 'gioi-thieu-spring-boot', 'VIDEO', NULL, 600, TRUE, TRUE, 1),
(2, 1, 'Tạo project Spring Boot đầu tiên', 'tao-project-spring-boot-dau-tien', 'VIDEO', NULL, 1200, FALSE, TRUE, 2),
(3, 1, 'Ghi chú: Cấu trúc Maven và package', 'ghi-chu-cau-truc-maven-package', 'TEXT', '<h2>Cấu trúc Maven</h2><p>Nên chia theo module/controller/service/repository.</p>', 300, FALSE, TRUE, 3),
(4, 2, 'Xây dựng REST API CRUD', 'xay-dung-rest-api-crud', 'VIDEO', NULL, 1500, FALSE, TRUE, 1),
(5, 2, 'Quiz: Spring Boot Basics', 'quiz-spring-boot-basics', 'QUIZ', NULL, 900, FALSE, TRUE, 2),
(6, 2, 'Bài tập: Xây dựng API Course', 'bai-tap-xay-dung-api-course', 'ASSIGNMENT', NULL, 1800, FALSE, TRUE, 3),

(7, 3, 'Tổng quan Microservices', 'tong-quan-microservices', 'VIDEO', NULL, 900, TRUE, TRUE, 1),
(8, 3, 'Thiết kế service boundary', 'thiet-ke-service-boundary', 'VIDEO', NULL, 1500, FALSE, TRUE, 2),
(9, 4, 'API Gateway và Service Discovery', 'api-gateway-service-discovery', 'VIDEO', NULL, 1800, FALSE, TRUE, 1),
(10, 4, 'Quiz: Microservices Foundation', 'quiz-microservices-foundation', 'QUIZ', NULL, 900, FALSE, TRUE, 2),
(11, 4, 'Bài tập: Tách hệ thống course service', 'bai-tap-tach-course-service', 'ASSIGNMENT', NULL, 1800, FALSE, TRUE, 3),

(12, 5, 'Setup React TypeScript với Vite', 'setup-react-typescript-vite', 'VIDEO', NULL, 900, TRUE, TRUE, 1),
(13, 5, 'Component, Props và State', 'component-props-state', 'VIDEO', NULL, 1200, FALSE, TRUE, 2),
(14, 5, 'Ghi chú: Quy ước đặt tên component', 'ghi-chu-quy-uoc-component', 'TEXT', '<p>Dùng PascalCase cho component, camelCase cho function và biến.</p>', 300, FALSE, TRUE, 3),
(15, 6, 'API Layer với Fetch/Axios', 'api-layer-fetch-axios', 'VIDEO', NULL, 1500, FALSE, TRUE, 1),
(16, 6, 'Quiz: React TypeScript', 'quiz-react-typescript', 'QUIZ', NULL, 900, FALSE, TRUE, 2),
(17, 6, 'Bài tập: Build Course Card Component', 'bai-tap-course-card-component', 'ASSIGNMENT', NULL, 600, FALSE, TRUE, 3),

(18, 7, 'Làm sạch dữ liệu với Pandas', 'lam-sach-du-lieu-pandas', 'VIDEO', NULL, 1200, TRUE, TRUE, 1),
(19, 7, 'Feature Engineering cơ bản', 'feature-engineering-co-ban', 'VIDEO', NULL, 1500, FALSE, TRUE, 2),
(20, 8, 'Train/Test Split và Evaluation', 'train-test-split-evaluation', 'VIDEO', NULL, 1800, FALSE, TRUE, 1),
(21, 8, 'Quiz: Machine Learning Basics', 'quiz-machine-learning-basics', 'QUIZ', NULL, 900, FALSE, TRUE, 2),
(22, 8, 'Bài tập: Dự đoán giá nhà', 'bai-tap-du-doan-gia-nha', 'ASSIGNMENT', NULL, 1800, FALSE, TRUE, 3),

(23, 9, 'Nguyên lý visual hierarchy', 'nguyen-ly-visual-hierarchy', 'VIDEO', NULL, 900, TRUE, TRUE, 1),
(24, 9, 'Spacing, Typography và Color', 'spacing-typography-color', 'VIDEO', NULL, 1200, FALSE, TRUE, 2),
(25, 10, 'Thiết kế landing page trong Figma', 'thiet-ke-landing-page-figma', 'VIDEO', NULL, 1500, FALSE, TRUE, 1),
(26, 10, 'Quiz: UI/UX Foundation', 'quiz-uiux-foundation', 'QUIZ', NULL, 600, FALSE, TRUE, 2),
(27, 10, 'Bài tập: Thiết kế Course Detail Page', 'bai-tap-thiet-ke-course-detail-page', 'ASSIGNMENT', NULL, 600, FALSE, TRUE, 3),

(28, 11, 'Từ requirement đến ERD', 'tu-requirement-den-erd', 'VIDEO', NULL, 900, TRUE, TRUE, 1),
(29, 11, 'Chuẩn hóa database', 'chuan-hoa-database', 'VIDEO', NULL, 1200, FALSE, TRUE, 2),
(30, 12, 'Index và Query Optimization', 'index-query-optimization', 'VIDEO', NULL, 1500, FALSE, TRUE, 1),
(31, 12, 'Seed data và migration', 'seed-data-va-migration', 'VIDEO', NULL, 600, FALSE, TRUE, 2);

INSERT INTO lesson_videos (id, lesson_id, provider, asset_id, original_file_url, playback_url, hls_url, thumbnail_url, duration_seconds, size_bytes, mime_type, status) VALUES
(1, 1, 'R2', 'courses/1/lessons/1/master.m3u8', 'https://storage.skillora.vn/original/c1-l1.mp4', 'https://cdn.skillora.vn/courses/1/lessons/1/master.m3u8', 'https://cdn.skillora.vn/courses/1/lessons/1/master.m3u8', 'https://cdn.skillora.vn/courses/1/lessons/1/thumb.jpg', 600, 125000000, 'video/mp4', 'READY'),
(2, 2, 'R2', 'courses/1/lessons/2/master.m3u8', 'https://storage.skillora.vn/original/c1-l2.mp4', 'https://cdn.skillora.vn/courses/1/lessons/2/master.m3u8', 'https://cdn.skillora.vn/courses/1/lessons/2/master.m3u8', 'https://cdn.skillora.vn/courses/1/lessons/2/thumb.jpg', 1200, 260000000, 'video/mp4', 'READY'),
(3, 4, 'R2', 'courses/1/lessons/4/master.m3u8', 'https://storage.skillora.vn/original/c1-l4.mp4', 'https://cdn.skillora.vn/courses/1/lessons/4/master.m3u8', 'https://cdn.skillora.vn/courses/1/lessons/4/master.m3u8', 'https://cdn.skillora.vn/courses/1/lessons/4/thumb.jpg', 1500, 310000000, 'video/mp4', 'READY'),
(4, 7, 'MUX', 'mux_microservices_intro_001', NULL, 'https://stream.mux.com/microservices-intro.m3u8', 'https://stream.mux.com/microservices-intro.m3u8', 'https://image.mux.com/microservices-intro/thumbnail.jpg', 900, NULL, 'application/x-mpegURL', 'READY'),
(5, 8, 'MUX', 'mux_service_boundary_001', NULL, 'https://stream.mux.com/service-boundary.m3u8', 'https://stream.mux.com/service-boundary.m3u8', 'https://image.mux.com/service-boundary/thumbnail.jpg', 1500, NULL, 'application/x-mpegURL', 'READY'),
(6, 9, 'MUX', 'mux_gateway_discovery_001', NULL, 'https://stream.mux.com/gateway-discovery.m3u8', 'https://stream.mux.com/gateway-discovery.m3u8', 'https://image.mux.com/gateway-discovery/thumbnail.jpg', 1800, NULL, 'application/x-mpegURL', 'READY'),
(7, 12, 'R2', 'courses/3/lessons/12/master.m3u8', 'https://storage.skillora.vn/original/c3-l12.mp4', 'https://cdn.skillora.vn/courses/3/lessons/12/master.m3u8', 'https://cdn.skillora.vn/courses/3/lessons/12/master.m3u8', 'https://cdn.skillora.vn/courses/3/lessons/12/thumb.jpg', 900, 190000000, 'video/mp4', 'READY'),
(8, 13, 'R2', 'courses/3/lessons/13/master.m3u8', 'https://storage.skillora.vn/original/c3-l13.mp4', 'https://cdn.skillora.vn/courses/3/lessons/13/master.m3u8', 'https://cdn.skillora.vn/courses/3/lessons/13/master.m3u8', 'https://cdn.skillora.vn/courses/3/lessons/13/thumb.jpg', 1200, 250000000, 'video/mp4', 'READY'),
(9, 15, 'R2', 'courses/3/lessons/15/master.m3u8', 'https://storage.skillora.vn/original/c3-l15.mp4', 'https://cdn.skillora.vn/courses/3/lessons/15/master.m3u8', 'https://cdn.skillora.vn/courses/3/lessons/15/master.m3u8', 'https://cdn.skillora.vn/courses/3/lessons/15/thumb.jpg', 1500, 295000000, 'video/mp4', 'READY'),
(10, 18, 'R2', 'courses/4/lessons/18/master.m3u8', 'https://storage.skillora.vn/original/c4-l18.mp4', 'https://cdn.skillora.vn/courses/4/lessons/18/master.m3u8', 'https://cdn.skillora.vn/courses/4/lessons/18/master.m3u8', 'https://cdn.skillora.vn/courses/4/lessons/18/thumb.jpg', 1200, 260000000, 'video/mp4', 'READY'),
(11, 19, 'R2', 'courses/4/lessons/19/master.m3u8', 'https://storage.skillora.vn/original/c4-l19.mp4', 'https://cdn.skillora.vn/courses/4/lessons/19/master.m3u8', 'https://cdn.skillora.vn/courses/4/lessons/19/master.m3u8', 'https://cdn.skillora.vn/courses/4/lessons/19/thumb.jpg', 1500, 300000000, 'video/mp4', 'READY'),
(12, 20, 'R2', 'courses/4/lessons/20/master.m3u8', 'https://storage.skillora.vn/original/c4-l20.mp4', 'https://cdn.skillora.vn/courses/4/lessons/20/master.m3u8', 'https://cdn.skillora.vn/courses/4/lessons/20/master.m3u8', 'https://cdn.skillora.vn/courses/4/lessons/20/thumb.jpg', 1800, 380000000, 'video/mp4', 'READY'),
(13, 23, 'R2', 'courses/5/lessons/23/master.m3u8', 'https://storage.skillora.vn/original/c5-l23.mp4', 'https://cdn.skillora.vn/courses/5/lessons/23/master.m3u8', 'https://cdn.skillora.vn/courses/5/lessons/23/master.m3u8', 'https://cdn.skillora.vn/courses/5/lessons/23/thumb.jpg', 900, 170000000, 'video/mp4', 'READY'),
(14, 24, 'R2', 'courses/5/lessons/24/master.m3u8', 'https://storage.skillora.vn/original/c5-l24.mp4', 'https://cdn.skillora.vn/courses/5/lessons/24/master.m3u8', 'https://cdn.skillora.vn/courses/5/lessons/24/master.m3u8', 'https://cdn.skillora.vn/courses/5/lessons/24/thumb.jpg', 1200, 240000000, 'video/mp4', 'READY'),
(15, 25, 'R2', 'courses/5/lessons/25/master.m3u8', 'https://storage.skillora.vn/original/c5-l25.mp4', 'https://cdn.skillora.vn/courses/5/lessons/25/master.m3u8', 'https://cdn.skillora.vn/courses/5/lessons/25/master.m3u8', 'https://cdn.skillora.vn/courses/5/lessons/25/thumb.jpg', 1500, 305000000, 'video/mp4', 'READY'),
(16, 28, 'R2', 'courses/6/lessons/28/master.m3u8', 'https://storage.skillora.vn/original/c6-l28.mp4', 'https://cdn.skillora.vn/courses/6/lessons/28/master.m3u8', 'https://cdn.skillora.vn/courses/6/lessons/28/master.m3u8', 'https://cdn.skillora.vn/courses/6/lessons/28/thumb.jpg', 900, 180000000, 'video/mp4', 'READY'),
(17, 29, 'R2', 'courses/6/lessons/29/master.m3u8', 'https://storage.skillora.vn/original/c6-l29.mp4', 'https://cdn.skillora.vn/courses/6/lessons/29/master.m3u8', 'https://cdn.skillora.vn/courses/6/lessons/29/master.m3u8', 'https://cdn.skillora.vn/courses/6/lessons/29/thumb.jpg', 1200, 220000000, 'video/mp4', 'READY'),
(18, 30, 'R2', 'courses/6/lessons/30/master.m3u8', 'https://storage.skillora.vn/original/c6-l30.mp4', 'https://cdn.skillora.vn/courses/6/lessons/30/master.m3u8', 'https://cdn.skillora.vn/courses/6/lessons/30/master.m3u8', 'https://cdn.skillora.vn/courses/6/lessons/30/thumb.jpg', 1500, 320000000, 'video/mp4', 'READY'),
(19, 31, 'R2', 'courses/6/lessons/31/master.m3u8', NULL, NULL, NULL, NULL, 600, NULL, 'video/mp4', 'PROCESSING');

INSERT INTO lesson_video_variants (id, lesson_video_id, quality, width, height, bitrate, url, size_bytes) VALUES
(1, 1, '720p', 1280, 720, 2500, 'https://cdn.skillora.vn/courses/1/lessons/1/720p.m3u8', 80000000),
(2, 1, '1080p', 1920, 1080, 4500, 'https://cdn.skillora.vn/courses/1/lessons/1/1080p.m3u8', 125000000),
(3, 2, '720p', 1280, 720, 2500, 'https://cdn.skillora.vn/courses/1/lessons/2/720p.m3u8', 160000000),
(4, 2, '1080p', 1920, 1080, 4500, 'https://cdn.skillora.vn/courses/1/lessons/2/1080p.m3u8', 260000000),
(5, 7, '720p', 1280, 720, 2500, 'https://cdn.skillora.vn/courses/3/lessons/12/720p.m3u8', 120000000),
(6, 10, '720p', 1280, 720, 2500, 'https://cdn.skillora.vn/courses/4/lessons/18/720p.m3u8', 150000000),
(7, 13, '720p', 1280, 720, 2500, 'https://cdn.skillora.vn/courses/5/lessons/23/720p.m3u8', 110000000),
(8, 16, '720p', 1280, 720, 2500, 'https://cdn.skillora.vn/courses/6/lessons/28/720p.m3u8', 115000000);

INSERT INTO lesson_resources (id, lesson_id, name, file_url, resource_type, size_bytes, order_index) VALUES
(1, 1, 'Slide giới thiệu Spring Boot', 'https://cdn.skillora.vn/resources/c1-l1-slide.pdf', 'PDF', 2400000, 1),
(2, 2, 'Source code starter project', 'https://cdn.skillora.vn/resources/c1-l2-starter.zip', 'ZIP', 5200000, 1),
(3, 4, 'Postman collection CRUD API', 'https://cdn.skillora.vn/resources/c1-l4-postman.json', 'CODE', 45000, 1),
(4, 12, 'React TypeScript starter template', 'https://cdn.skillora.vn/resources/c3-l12-template.zip', 'ZIP', 3900000, 1),
(5, 18, 'Dataset demo sales.csv', 'https://cdn.skillora.vn/resources/c4-l18-sales.csv', 'OTHER', 800000, 1),
(6, 23, 'UI checklist PDF', 'https://cdn.skillora.vn/resources/c5-l23-ui-checklist.pdf', 'PDF', 1200000, 1),
(7, 28, 'ERD sample file', 'https://cdn.skillora.vn/resources/c6-l28-erd.drawio', 'OTHER', 300000, 1);


-- 04. CART / WISHLIST / ORDERS / PAYMENTS

INSERT INTO wishlists (user_id, course_id, created_at) VALUES
(5, 3, '2026-05-10 08:00:00'),
(5, 4, '2026-05-11 08:00:00'),
(6, 1, '2026-05-12 09:00:00'),
(7, 5, '2026-05-13 10:00:00'),
(8, 3, '2026-05-14 11:00:00');

INSERT INTO carts (id, user_id, created_at) VALUES
(1, 5, '2026-05-21 08:00:00'),
(2, 6, '2026-05-21 09:00:00'),
(3, 7, '2026-05-21 10:00:00'),
(4, 8, '2026-05-21 11:00:00');

INSERT INTO cart_items (cart_id, course_id, added_at) VALUES
(1, 5, '2026-05-21 08:05:00'),
(1, 6, '2026-05-21 08:06:00'),
(2, 4, '2026-05-21 09:05:00'),
(3, 2, '2026-05-21 10:05:00'),
(4, 1, '2026-05-21 11:05:00');

INSERT INTO coupons (id, code, name, discount_type, discount_value, max_uses, used_count, min_order_amount, starts_at, expires_at, active) VALUES
(1, 'WELCOME20', 'Ưu đãi chào mừng học viên mới', 'PERCENT', 20.00, 1000, 2, 100000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
(2, 'SKILLORA50K', 'Giảm trực tiếp 50.000đ', 'FIXED', 50000.00, 500, 1, 200000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
(3, 'EXPIRED10', 'Mã hết hạn demo', 'PERCENT', 10.00, 100, 0, NULL, '2025-01-01 00:00:00', '2025-12-31 23:59:59', FALSE);

INSERT INTO orders (id, user_id, coupon_id, subtotal_amount, discount_amount, total_amount, currency, status, payment_gateway, gateway_transaction_id, paid_at, failure_reason, created_at) VALUES
(1, 5, 1, 199000.00, 39800.00, 159200.00, 'VND', 'PAID', 'MOMO', 'MOMO_SKILLORA_0001', '2026-05-01 09:05:00', NULL, '2026-05-01 09:00:00'),
(2, 5, 2, 249000.00, 50000.00, 199000.00, 'VND', 'PAID', 'VNPAY', 'VNPAY_SKILLORA_0002', '2026-05-03 10:05:00', NULL, '2026-05-03 10:00:00'),
(3, 6, NULL, 199000.00, 0.00, 199000.00, 'VND', 'PAID', 'MOMO', 'MOMO_SKILLORA_0003', '2026-05-05 11:05:00', NULL, '2026-05-05 11:00:00'),
(4, 6, NULL, 349000.00, 0.00, 349000.00, 'VND', 'PAID', 'STRIPE', 'STRIPE_SKILLORA_0004', '2026-05-07 14:05:00', NULL, '2026-05-07 14:00:00'),
(5, 7, NULL, 249000.00, 0.00, 249000.00, 'VND', 'PAID', 'VNPAY', 'VNPAY_SKILLORA_0005', '2026-05-09 15:05:00', NULL, '2026-05-09 15:00:00'),
(6, 7, NULL, 399000.00, 0.00, 399000.00, 'VND', 'PAID', 'PAYPAL', 'PAYPAL_SKILLORA_0006', '2026-05-11 16:05:00', NULL, '2026-05-11 16:00:00'),
(7, 8, NULL, 159000.00, 0.00, 159000.00, 'VND', 'PAID', 'MOMO', 'MOMO_SKILLORA_0007', '2026-05-13 17:05:00', NULL, '2026-05-13 17:00:00'),
(8, 8, NULL, 199000.00, 0.00, 199000.00, 'VND', 'FAILED', 'MOMO', NULL, NULL, 'Insufficient balance', '2026-05-15 18:00:00');

INSERT INTO order_items (id, order_id, course_id, price, discount_amount, final_price, course_title_snapshot) VALUES
(1, 1, 1, 199000.00, 39800.00, 159200.00, 'Java Spring Boot từ Zero đến Hero'),
(2, 2, 3, 249000.00, 50000.00, 199000.00, 'ReactJS & TypeScript Web Hiện Đại'),
(3, 3, 1, 199000.00, 0.00, 199000.00, 'Java Spring Boot từ Zero đến Hero'),
(4, 4, 4, 349000.00, 0.00, 349000.00, 'Machine Learning với Python Thực Chiến'),
(5, 5, 3, 249000.00, 0.00, 249000.00, 'ReactJS & TypeScript Web Hiện Đại'),
(6, 6, 2, 399000.00, 0.00, 399000.00, 'Microservices với Spring Cloud'),
(7, 7, 5, 159000.00, 0.00, 159000.00, 'UI/UX Design với Figma cho Developer'),
(8, 8, 1, 199000.00, 0.00, 199000.00, 'Java Spring Boot từ Zero đến Hero');

INSERT INTO payment_transactions (id, order_id, request_id, gateway, amount, currency, status, result_code, message, gateway_order_id, gateway_transaction_id, pay_type, pay_url, raw_request, raw_response, response_time, created_at) VALUES
(1, 1, 'REQ_0001', 'MOMO', 159200.00, 'VND', 'SUCCESS', '0', 'Payment successful', 'ORDER_0001', 'MOMO_SKILLORA_0001', 'QR', NULL, JSON_OBJECT('orderId', 'ORDER_0001'), JSON_OBJECT('resultCode', 0), 350, '2026-05-01 09:05:00'),
(2, 2, 'REQ_0002', 'VNPAY', 199000.00, 'VND', 'SUCCESS', '00', 'Payment successful', 'ORDER_0002', 'VNPAY_SKILLORA_0002', 'BANK_CARD', NULL, JSON_OBJECT('orderId', 'ORDER_0002'), JSON_OBJECT('responseCode', '00'), 420, '2026-05-03 10:05:00'),
(3, 3, 'REQ_0003', 'MOMO', 199000.00, 'VND', 'SUCCESS', '0', 'Payment successful', 'ORDER_0003', 'MOMO_SKILLORA_0003', 'QR', NULL, JSON_OBJECT('orderId', 'ORDER_0003'), JSON_OBJECT('resultCode', 0), 330, '2026-05-05 11:05:00'),
(4, 4, 'REQ_0004', 'STRIPE', 349000.00, 'VND', 'SUCCESS', 'succeeded', 'Payment successful', 'ORDER_0004', 'STRIPE_SKILLORA_0004', 'CARD', NULL, JSON_OBJECT('orderId', 'ORDER_0004'), JSON_OBJECT('status', 'succeeded'), 510, '2026-05-07 14:05:00'),
(5, 5, 'REQ_0005', 'VNPAY', 249000.00, 'VND', 'SUCCESS', '00', 'Payment successful', 'ORDER_0005', 'VNPAY_SKILLORA_0005', 'BANK_CARD', NULL, JSON_OBJECT('orderId', 'ORDER_0005'), JSON_OBJECT('responseCode', '00'), 390, '2026-05-09 15:05:00'),
(6, 6, 'REQ_0006', 'PAYPAL', 399000.00, 'VND', 'SUCCESS', 'COMPLETED', 'Payment successful', 'ORDER_0006', 'PAYPAL_SKILLORA_0006', 'PAYPAL', NULL, JSON_OBJECT('orderId', 'ORDER_0006'), JSON_OBJECT('status', 'COMPLETED'), 610, '2026-05-11 16:05:00'),
(7, 7, 'REQ_0007', 'MOMO', 159000.00, 'VND', 'SUCCESS', '0', 'Payment successful', 'ORDER_0007', 'MOMO_SKILLORA_0007', 'QR', NULL, JSON_OBJECT('orderId', 'ORDER_0007'), JSON_OBJECT('resultCode', 0), 300, '2026-05-13 17:05:00'),
(8, 8, 'REQ_0008', 'MOMO', 199000.00, 'VND', 'FAILED', '1006', 'Insufficient balance', 'ORDER_0008', NULL, 'QR', 'https://pay.demo/momo/ORDER_0008', JSON_OBJECT('orderId', 'ORDER_0008'), JSON_OBJECT('resultCode', 1006), 280, '2026-05-15 18:05:00');


-- 05. ENROLLMENTS / PROGRESS / CERTIFICATES

INSERT INTO enrollments (id, user_id, course_id, order_item_id, status, amount_paid, progress_percent, enrolled_at, completed_at) VALUES
(1, 5, 1, 1, 'COMPLETED', 159200.00, 100.00, '2026-05-01 09:06:00', '2026-05-15 20:00:00'),
(2, 5, 3, 2, 'ACTIVE', 199000.00, 66.67, '2026-05-03 10:06:00', NULL),
(3, 6, 1, 3, 'ACTIVE', 199000.00, 50.00, '2026-05-05 11:06:00', NULL),
(4, 6, 4, 4, 'ACTIVE', 349000.00, 33.33, '2026-05-07 14:06:00', NULL),
(5, 7, 3, 5, 'COMPLETED', 249000.00, 100.00, '2026-05-09 15:06:00', '2026-05-20 21:00:00'),
(6, 7, 2, 6, 'ACTIVE', 399000.00, 40.00, '2026-05-11 16:06:00', NULL),
(7, 8, 5, 7, 'ACTIVE', 159000.00, 60.00, '2026-05-13 17:06:00', NULL);

INSERT INTO lesson_progress (id, enrollment_id, lesson_id, watched_seconds, completed, completed_at, last_accessed_at) VALUES
(1, 1, 1, 600, TRUE, '2026-05-02 09:00:00', '2026-05-02 09:00:00'),
(2, 1, 2, 1200, TRUE, '2026-05-04 09:00:00', '2026-05-04 09:00:00'),
(3, 1, 3, 300, TRUE, '2026-05-06 09:00:00', '2026-05-06 09:00:00'),
(4, 1, 4, 1500, TRUE, '2026-05-10 09:00:00', '2026-05-10 09:00:00'),
(5, 1, 5, 900, TRUE, '2026-05-12 09:00:00', '2026-05-12 09:00:00'),
(6, 1, 6, 1800, TRUE, '2026-05-15 20:00:00', '2026-05-15 20:00:00'),
(7, 2, 12, 900, TRUE, '2026-05-04 10:30:00', '2026-05-04 10:30:00'),
(8, 2, 13, 1200, TRUE, '2026-05-06 10:30:00', '2026-05-06 10:30:00'),
(9, 2, 14, 300, TRUE, '2026-05-07 10:30:00', '2026-05-07 10:30:00'),
(10, 2, 15, 800, FALSE, NULL, '2026-05-21 10:30:00'),
(11, 3, 1, 600, TRUE, '2026-05-06 12:00:00', '2026-05-06 12:00:00'),
(12, 3, 2, 1200, TRUE, '2026-05-08 12:00:00', '2026-05-08 12:00:00'),
(13, 3, 3, 300, TRUE, '2026-05-10 12:00:00', '2026-05-10 12:00:00'),
(14, 3, 4, 500, FALSE, NULL, '2026-05-21 12:00:00'),
(15, 4, 18, 1200, TRUE, '2026-05-09 15:00:00', '2026-05-09 15:00:00'),
(16, 4, 19, 700, FALSE, NULL, '2026-05-21 15:00:00'),
(17, 5, 12, 900, TRUE, '2026-05-10 15:30:00', '2026-05-10 15:30:00'),
(18, 5, 13, 1200, TRUE, '2026-05-12 15:30:00', '2026-05-12 15:30:00'),
(19, 5, 14, 300, TRUE, '2026-05-13 15:30:00', '2026-05-13 15:30:00'),
(20, 5, 15, 1500, TRUE, '2026-05-16 15:30:00', '2026-05-16 15:30:00'),
(21, 5, 16, 900, TRUE, '2026-05-18 15:30:00', '2026-05-18 15:30:00'),
(22, 5, 17, 600, TRUE, '2026-05-20 21:00:00', '2026-05-20 21:00:00'),
(23, 6, 7, 900, TRUE, '2026-05-12 16:30:00', '2026-05-12 16:30:00'),
(24, 6, 8, 1500, TRUE, '2026-05-14 16:30:00', '2026-05-14 16:30:00'),
(25, 7, 23, 900, TRUE, '2026-05-14 17:30:00', '2026-05-14 17:30:00'),
(26, 7, 24, 1200, TRUE, '2026-05-16 17:30:00', '2026-05-16 17:30:00'),
(27, 7, 25, 1100, FALSE, NULL, '2026-05-22 17:30:00');

INSERT INTO course_certificates (id, enrollment_id, certificate_code, pdf_url, issued_at) VALUES
(1, 1, 'SKILLORA-CERT-2026-000001', 'https://cdn.skillora.vn/certificates/SKILLORA-CERT-2026-000001.pdf', '2026-05-15 20:05:00'),
(2, 5, 'SKILLORA-CERT-2026-000002', 'https://cdn.skillora.vn/certificates/SKILLORA-CERT-2026-000002.pdf', '2026-05-20 21:05:00');


-- 06. QUIZZES / QUESTIONS / ATTEMPTS

INSERT INTO quizzes (id, lesson_id, title, description, pass_score, time_limit_mins, max_attempts, shuffle_questions) VALUES
(1, 5, 'Spring Boot Basics Quiz', 'Kiểm tra kiến thức nền tảng Spring Boot.', 70, 15, 3, TRUE),
(2, 10, 'Microservices Foundation Quiz', 'Kiểm tra kiến thức về microservices.', 70, 15, 3, TRUE),
(3, 16, 'React TypeScript Quiz', 'Kiểm tra kiến thức React và TypeScript.', 70, 15, 3, TRUE),
(4, 21, 'Machine Learning Basics Quiz', 'Kiểm tra kiến thức Machine Learning cơ bản.', 70, 20, 3, TRUE),
(5, 26, 'UI/UX Foundation Quiz', 'Kiểm tra kiến thức UI/UX cơ bản.', 70, 10, 3, TRUE);

INSERT INTO questions (id, quiz_id, content, type, points, order_index, explanation) VALUES
(1, 1, 'Annotation nào thường dùng để khai báo một REST controller trong Spring Boot?', 'SINGLE', 10, 1, '@RestController kết hợp @Controller và @ResponseBody.'),
(2, 1, 'Các thành phần nào thường nằm trong kiến trúc backend Spring Boot?', 'MULTIPLE', 10, 2, 'Controller, Service và Repository là các layer phổ biến.'),
(3, 1, 'Spring Boot có thể chạy embedded server như Tomcat.', 'TRUE_FALSE', 10, 3, 'Spring Boot hỗ trợ embedded Tomcat mặc định.'),
(4, 2, 'API Gateway thường dùng để làm gì?', 'SINGLE', 10, 1, 'Gateway là cửa ngõ routing request vào các service.'),
(5, 2, 'Microservices luôn đơn giản hơn monolith trong mọi trường hợp.', 'TRUE_FALSE', 10, 2, 'Microservices tăng độ phức tạp vận hành.'),
(6, 3, 'Props trong React dùng để làm gì?', 'SINGLE', 10, 1, 'Props truyền dữ liệu từ component cha xuống component con.'),
(7, 3, 'TypeScript giúp ích gì trong project React?', 'MULTIPLE', 10, 2, 'TypeScript giúp type checking, autocomplete và giảm lỗi runtime.'),
(8, 4, 'Train/test split dùng để làm gì?', 'SINGLE', 10, 1, 'Dùng để đánh giá model trên dữ liệu chưa thấy khi train.'),
(9, 4, 'Overfitting nghĩa là mô hình học quá khớp dữ liệu train.', 'TRUE_FALSE', 10, 2, 'Overfitting khiến model kém tổng quát hóa.'),
(10, 5, 'Visual hierarchy giúp người dùng làm gì?', 'SINGLE', 10, 1, 'Giúp người dùng hiểu mức độ ưu tiên của nội dung.'),
(11, 5, 'Spacing và typography có ảnh hưởng đến trải nghiệm người dùng.', 'TRUE_FALSE', 10, 2, 'Hai yếu tố này ảnh hưởng mạnh đến readability và perception.');

INSERT INTO answer_options (id, question_id, content, is_correct, order_index) VALUES
(1, 1, '@Service', FALSE, 1),
(2, 1, '@Repository', FALSE, 2),
(3, 1, '@RestController', TRUE, 3),
(4, 1, '@Entity', FALSE, 4),
(5, 2, 'Controller', TRUE, 1),
(6, 2, 'Service', TRUE, 2),
(7, 2, 'Repository', TRUE, 3),
(8, 2, 'HTML Template bắt buộc', FALSE, 4),
(9, 3, 'True', TRUE, 1),
(10, 3, 'False', FALSE, 2),
(11, 4, 'Routing request đến các service phía sau', TRUE, 1),
(12, 4, 'Thay thế hoàn toàn database', FALSE, 2),
(13, 4, 'Tự động viết code frontend', FALSE, 3),
(14, 5, 'True', FALSE, 1),
(15, 5, 'False', TRUE, 2),
(16, 6, 'Truyền dữ liệu từ cha xuống con', TRUE, 1),
(17, 6, 'Lưu dữ liệu server', FALSE, 2),
(18, 6, 'Thay thế CSS', FALSE, 3),
(19, 7, 'Type checking', TRUE, 1),
(20, 7, 'Autocomplete tốt hơn', TRUE, 2),
(21, 7, 'Giảm một số lỗi runtime', TRUE, 3),
(22, 7, 'Bắt buộc không cần build app', FALSE, 4),
(23, 8, 'Đánh giá model trên dữ liệu kiểm thử', TRUE, 1),
(24, 8, 'Xóa toàn bộ dữ liệu xấu', FALSE, 2),
(25, 8, 'Tăng kích thước file model', FALSE, 3),
(26, 9, 'True', TRUE, 1),
(27, 9, 'False', FALSE, 2),
(28, 10, 'Hiểu thứ tự ưu tiên của nội dung', TRUE, 1),
(29, 10, 'Tăng tốc database', FALSE, 2),
(30, 10, 'Tự động tạo ảnh', FALSE, 3),
(31, 11, 'True', TRUE, 1),
(32, 11, 'False', FALSE, 2);

INSERT INTO quiz_attempts (id, enrollment_id, quiz_id, user_id, attempt_no, score, passed, started_at, submitted_at) VALUES
(1, 1, 1, 5, 1, 100.00, TRUE, '2026-05-12 08:30:00', '2026-05-12 08:40:00'),
(2, 5, 3, 7, 1, 100.00, TRUE, '2026-05-18 15:00:00', '2026-05-18 15:08:00'),
(3, 7, 5, 8, 1, 50.00, FALSE, '2026-05-21 18:00:00', '2026-05-21 18:06:00');

INSERT INTO quiz_attempt_answers (id, attempt_id, question_id, text_answer, is_correct, points_earned) VALUES
(1, 1, 1, NULL, TRUE, 10),
(2, 1, 2, NULL, TRUE, 10),
(3, 1, 3, NULL, TRUE, 10),
(4, 2, 6, NULL, TRUE, 10),
(5, 2, 7, NULL, TRUE, 10),
(6, 3, 10, NULL, TRUE, 10),
(7, 3, 11, NULL, FALSE, 0);

INSERT INTO quiz_attempt_answer_options (attempt_answer_id, option_id) VALUES
(1, 3),
(2, 5), (2, 6), (2, 7),
(3, 9),
(4, 16),
(5, 19), (5, 20), (5, 21),
(6, 28),
(7, 32);


-- 07. ASSIGNMENTS

INSERT INTO assignments (id, lesson_id, title, instructions, max_score, due_days) VALUES
(1, 6, 'Xây dựng API Course bằng Spring Boot', 'Tạo CRUD API cho Course gồm create, update, list, detail và delete mềm.', 100, 14),
(2, 11, 'Tách Course Service trong hệ thống microservices', 'Thiết kế service boundary, API contract và database riêng cho course service.', 100, 14),
(3, 17, 'Build Course Card Component', 'Tạo component CourseCard bằng React TypeScript, hỗ trợ thumbnail, title, instructor, price và rating.', 100, 7),
(4, 22, 'Dự đoán giá nhà với scikit-learn', 'Dùng dataset mẫu để train model regression và viết báo cáo ngắn về kết quả.', 100, 14),
(5, 27, 'Thiết kế Course Detail Page', 'Thiết kế màn hình chi tiết khóa học trong Figma, có hero, curriculum, review và CTA.', 100, 7);

INSERT INTO assignment_submissions (id, assignment_id, enrollment_id, content, file_url, score, feedback, status, submitted_at, graded_at, graded_by) VALUES
(1, 1, 1, 'Em đã hoàn thành API Course và đẩy source code lên GitHub.', 'https://github.com/demo/skillora-course-api', 92.00, 'Bài làm tốt, cần bổ sung validation chi tiết hơn.', 'GRADED', '2026-05-14 20:00:00', '2026-05-15 08:00:00', 2),
(2, 3, 5, 'Em đã hoàn thành CourseCard Component.', 'https://github.com/demo/course-card-component', 95.00, 'Component rõ ràng, responsive tốt.', 'GRADED', '2026-05-19 20:00:00', '2026-05-20 08:00:00', 3);


-- 08. REVIEWS / SOCIAL

INSERT INTO reviews (id, enrollment_id, user_id, course_id, rating, content, status, created_at) VALUES
(1, 1, 5, 1, 5, 'Khóa học Spring Boot rất dễ hiểu, có ví dụ thực tế và bài tập rõ ràng.', 'PUBLISHED', '2026-05-16 09:00:00'),
(2, 3, 6, 1, 4, 'Nội dung tốt, phần security hơi nhanh nhưng vẫn theo được.', 'PUBLISHED', '2026-05-18 10:00:00'),
(3, 2, 5, 3, 4, 'React TypeScript giải thích rõ, project structure khá thực tế.', 'PUBLISHED', '2026-05-19 11:00:00'),
(4, 5, 7, 3, 5, 'Khóa React rất phù hợp để làm project portfolio.', 'PUBLISHED', '2026-05-21 12:00:00'),
(5, 4, 6, 4, 4, 'Phần pandas và train model dễ hiểu, cần thêm nhiều dataset hơn.', 'PUBLISHED', '2026-05-20 13:00:00'),
(6, 6, 7, 2, 5, 'Microservices giải thích rất chắc, có nhiều ví dụ thực tế.', 'PUBLISHED', '2026-05-21 14:00:00'),
(7, 7, 8, 5, 5, 'UI/UX cho developer rất dễ áp dụng vào project web.', 'PUBLISHED', '2026-05-22 08:00:00');

INSERT INTO review_likes (user_id, review_id, created_at) VALUES
(6, 1, '2026-05-16 10:00:00'),
(7, 1, '2026-05-16 10:05:00'),
(5, 2, '2026-05-18 11:00:00'),
(8, 4, '2026-05-21 13:00:00'),
(5, 7, '2026-05-22 09:00:00');

-- 09. AI CHAT / NOTIFICATIONS

INSERT INTO chat_conversations (id, user_id, course_id, title, created_at, updated_at) VALUES
(1, 5, 1, 'Hỏi đáp Spring Boot Security', '2026-05-12 20:00:00', '2026-05-12 20:05:00'),
(2, 6, 4, 'Hỏi đáp Machine Learning', '2026-05-18 21:00:00', '2026-05-18 21:07:00'),
(3, 7, 3, 'Hỏi đáp React TypeScript', '2026-05-19 22:00:00', '2026-05-19 22:04:00');

INSERT INTO chat_messages (id, conversation_id, role, content, model, tokens_used, created_at) VALUES
(1, 1, 'USER', 'JWT trong Spring Security hoạt động như thế nào?', NULL, NULL, '2026-05-12 20:00:00'),
(2, 1, 'ASSISTANT', 'JWT là token chứa claims, backend verify chữ ký để xác thực request mà không cần lưu session server-side.', 'gpt-4.1-mini', 120, '2026-05-12 20:01:00'),
(3, 2, 'USER', 'Overfitting là gì?', NULL, NULL, '2026-05-18 21:00:00'),
(4, 2, 'ASSISTANT', 'Overfitting xảy ra khi model học quá kỹ dữ liệu train và hoạt động kém trên dữ liệu mới.', 'gpt-4.1-mini', 95, '2026-05-18 21:01:00'),
(5, 3, 'USER', 'Khi nào nên dùng custom hook?', NULL, NULL, '2026-05-19 22:00:00'),
(6, 3, 'ASSISTANT', 'Nên dùng custom hook khi bạn muốn tái sử dụng logic state/effect giữa nhiều component.', 'gpt-4.1-mini', 88, '2026-05-19 22:01:00');

INSERT INTO notifications (id, user_id, type, title, content, data, read_at, created_at) VALUES
(1, 5, 'COURSE_ENROLLED', 'Bạn đã đăng ký khóa học thành công', 'Khóa Java Spring Boot từ Zero đến Hero đã được thêm vào học tập của bạn.', JSON_OBJECT('courseId', 1), '2026-05-01 10:00:00', '2026-05-01 09:06:00'),
(2, 5, 'CERTIFICATE_ISSUED', 'Bạn đã nhận chứng chỉ', 'Chúc mừng bạn đã hoàn thành khóa Java Spring Boot.', JSON_OBJECT('certificateCode', 'SKILLORA-CERT-2026-000001'), NULL, '2026-05-15 20:05:00'),
(3, 7, 'ASSIGNMENT_GRADED', 'Bài tập đã được chấm', 'Bài tập Course Card Component của bạn đạt 95 điểm.', JSON_OBJECT('assignmentId', 3, 'score', 95), NULL, '2026-05-20 08:00:00'),
(4, 8, 'PAYMENT_FAILED', 'Thanh toán thất bại', 'Giao dịch thanh toán khóa Java Spring Boot chưa thành công.', JSON_OBJECT('orderId', 8), NULL, '2026-05-15 18:05:00');

-- 10. STATS / AUDIT

INSERT INTO course_stats (course_id, total_enrollments, total_reviews, avg_rating, total_completions, total_revenue, total_lessons, total_duration_seconds, last_enrolled_at) VALUES
(1, 2, 2, 4.50, 1, 358200.00, 6, 6300, '2026-05-05 11:06:00'),
(2, 1, 1, 5.00, 0, 399000.00, 5, 6900, '2026-05-11 16:06:00'),
(3, 2, 2, 4.50, 1, 448000.00, 6, 5400, '2026-05-09 15:06:00'),
(4, 1, 1, 4.00, 0, 349000.00, 6, 7200, '2026-05-07 14:06:00'),
(5, 1, 1, 5.00, 0, 159000.00, 5, 4800, '2026-05-13 17:06:00'),
(6, 0, 0, 0.00, 0, 0.00, 4, 4200, NULL);

INSERT INTO audit_logs (id, actor_id, entity_type, entity_id, action, old_values, new_values, ip_address, user_agent, created_at) VALUES
(1, 1, 'COURSE', 1, 'PUBLISH', JSON_OBJECT('status', 'REVIEWING'), JSON_OBJECT('status', 'PUBLISHED'), '127.0.0.1', 'Chrome Windows', '2026-02-01 08:00:00'),
(2, 1, 'COURSE', 3, 'PUBLISH', JSON_OBJECT('status', 'REVIEWING'), JSON_OBJECT('status', 'PUBLISHED'), '127.0.0.1', 'Chrome Windows', '2026-02-15 08:00:00'),
(3, 2, 'LESSON_VIDEO', 19, 'UPLOAD_STARTED', NULL, JSON_OBJECT('status', 'PROCESSING'), '127.0.0.1', 'Chrome Windows', '2026-04-01 10:00:00');

COMMIT;


-- OPTIONAL QUICK CHECKS
-- SELECT COUNT(*) AS total_users FROM users;
-- SELECT COUNT(*) AS total_courses FROM courses;
-- SELECT COUNT(*) AS total_lessons FROM lessons;
-- SELECT COUNT(*) AS total_enrollments FROM enrollments;
-- SELECT * FROM v_course_detail ORDER BY id;
-- SELECT * FROM v_enrollment_progress ORDER BY enrollment_id;
