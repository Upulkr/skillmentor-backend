-- Add review table
-- V2__Add_Review_Table.sql

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    mentor_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    review_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT fk_review_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_review_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id),
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5)
);
