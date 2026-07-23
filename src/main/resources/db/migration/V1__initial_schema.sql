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
