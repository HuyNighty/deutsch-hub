-- DeutschHub
-- Version 1
-- Initial Schema

-- IDENTITY CONTEXT V1
CREATE TABLE users (
    id VARCHAR(36) NOT NULL,

    username VARCHAR(30) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    last_login_at DATETIME(6),

    CONSTRAINT pk_users
        PRIMARY KEY (id),

    CONSTRAINT uk_users_username
        UNIQUE (username),

    CONSTRAINT uk_users_email
        UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,

    CONSTRAINT pk_user_roles
        PRIMARY KEY (user_id, role),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE user_sessions (
    id VARCHAR(36) NOT NULL,

    user_id VARCHAR(36) NOT NULL,

    refresh_hash_token VARCHAR(255) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6),

    CONSTRAINT pk_user_sessions
        PRIMARY KEY (id),

    CONSTRAINT uk_user_sessions_refresh_hash_token
        UNIQUE (refresh_hash_token),

    CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user_id
ON user_sessions(user_id);

--LEARNING CONTEXT V1
CREATE TABLE courses (
    id VARCHAR(36) NOT NULL,

    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    level VARCHAR(3) NOT NULL,

    price_amount DECIMAL(38,2) NOT NULL,

    price_currency VARCHAR(3) NOT NULL,

    published BOOLEAN NOT NULL DEFAULT FALSE,

    instructor_id VARCHAR(36) NOT NULL,

    estimated_hours INT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),

    CONSTRAINT pk_courses
        PRIMARY KEY (id),

    CONSTRAINT fk_courses_instructor
           FOREIGN KEY (instructor_id)
           REFERENCES users(id)
);

CREATE TABLE course_sections (
    id VARCHAR(36) NOT NULL,

    course_id VARCHAR(36) NOT NULL,

    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),

    order_index INT NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),

    CONSTRAINT pk_course_sections
        PRIMARY KEY (id),

    CONSTRAINT uk_course_sections_course_order_index
        UNIQUE (course_id, order_index),

    CONSTRAINT fk_course_sections_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
);

CREATE TABLE course_lessons (
    id VARCHAR(36) NOT NULL,

    section_id VARCHAR(36) NOT NULL,

    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),

    estimated_minutes INT NOT NULL,
    level VARCHAR(3) NOT NULL,
    order_index INT NOT NULL,

    free_preview BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),

    CONSTRAINT pk_course_lessons
        PRIMARY KEY (id),

    CONSTRAINT uk_course_lessons_section_order_index
        UNIQUE (section_id, order_index),

    CONSTRAINT fk_course_lessons_section
        FOREIGN KEY (section_id)
        REFERENCES course_sections(id)
        ON DELETE CASCADE
);

CREATE TABLE lesson_items (
    id VARCHAR(36) NOT NULL,

    lesson_id VARCHAR(36) NOT NULL,
    quiz_id VARCHAR(36),

    type VARCHAR(12) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    content TEXT,
    resource_url VARCHAR(255),

    estimated_minutes INT NOT NULL,
    order_index INT NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),

    CONSTRAINT pk_lesson_items
        PRIMARY KEY (id),

    CONSTRAINT uk_lesson_items_lesson_order_index
        UNIQUE (lesson_id, order_index),

    CONSTRAINT fk_lesson_items_lessons
        FOREIGN KEY (lesson_id)
        REFERENCES course_lessons(id)
        ON DELETE CASCADE
);

CREATE TABLE enrollments (
    id VARCHAR(36) NOT NULL,

    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,

    status VARCHAR(20) NOT NULL,

    completed_lessons INT NOT NULL,
    total_lessons INT NOT NULL,
    completion_percentage DECIMAL(5,2) NOT NULL,
    total_study_minutes INT NOT NULL,

    progress_last_updated_at DATETIME(6),
    enrolled_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6),
    dropped_at DATETIME(6),
    expired_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_enrollments
        PRIMARY KEY (id),

    CONSTRAINT uk_enrollments_user_course
        UNIQUE (user_id, course_id),

    CONSTRAINT fk_enrollments_users
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_enrollments_courses
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_enrollments_course_id
ON enrollments(course_id);

CREATE TABLE lesson_completions (
    id VARCHAR(36) NOT NULL,

    enrollment_id VARCHAR(36) NOT NULL,
    lesson_id VARCHAR(36) NOT NULL,

    completed_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_lesson_completions
        PRIMARY KEY (id),

    CONSTRAINT uk_lesson_completions_enrollment_lesson
        UNIQUE (enrollment_id, lesson_id),

    CONSTRAINT fk_lesson_completions_enrollment
        FOREIGN KEY (enrollment_id)
        REFERENCES enrollments(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_lesson_completions_lesson
        FOREIGN KEY (lesson_id)
        REFERENCES course_lessons(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_lesson_completions_lesson_id
ON lesson_completions(lesson_id);