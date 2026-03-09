-- Seed sample data for SkillMentor
-- V3__Seed_Data.sql

-- 1. Create Sample Users
INSERT INTO users (clerk_id, email, role) VALUES 
('user_admin_01', 'admin@skillmentor.com', 'ADMIN'),
('user_mentor_01', 'jdoe@mentor.com', 'MENTOR'),
('user_student_01', 'student@test.com', 'STUDENT')
ON CONFLICT (clerk_id) DO NOTHING;

-- 2. Create Sample Mentors
-- Note: Assuming IDs 1 and 2 for users based on insertion order
INSERT INTO mentors (user_id, first_name, last_name, phone, title, profession, company, experience_years, bio, is_certified, start_year)
SELECT id, 'John', 'Doe', '+1234567890', 'Senior Software Architect', 'Software Engineering', 'Google', 12, 'Expert in Spring Boot and System Design.', TRUE, 2012
FROM users WHERE email = 'jdoe@mentor.com'
ON CONFLICT (user_id) DO NOTHING;

-- 3. Create Sample Subjects
INSERT INTO subjects (mentor_id, name, description, image_url)
SELECT id, 'Advanced Spring Boot', 'Deep dive into microservices and security.', 'https://images.unsplash.com/photo-1587620962725-abab7fe55159'
FROM mentors WHERE first_name = 'John' AND last_name = 'Doe'
AND NOT EXISTS (SELECT 1 FROM subjects s JOIN mentors m ON s.mentor_id = m.id WHERE m.first_name = 'John' AND m.last_name = 'Doe' AND s.name = 'Advanced Spring Boot');

INSERT INTO subjects (mentor_id, name, description, image_url)
SELECT id, 'System Design Interviews', 'Mastering scalability and high availability.', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c'
FROM mentors WHERE first_name = 'John' AND last_name = 'Doe'
AND NOT EXISTS (SELECT 1 FROM subjects s JOIN mentors m ON s.mentor_id = m.id WHERE m.first_name = 'John' AND m.last_name = 'Doe' AND s.name = 'System Design Interviews');
