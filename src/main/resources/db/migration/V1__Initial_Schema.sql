-- Create tables for SkillMentor Initial Schema
-- V1__Initial_Schema.sql

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    clerk_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mentors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    title VARCHAR(100) NOT NULL,
    profession VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    experience_years INTEGER NOT NULL,
    bio TEXT,
    profile_image_url VARCHAR(255),
    is_certified BOOLEAN NOT NULL DEFAULT FALSE,
    start_year INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mentor_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subject_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id)
);

CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    mentor_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    session_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    meeting_link VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_session_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id),
    CONSTRAINT fk_session_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);
