-- V4__Add_User_Profile_Columns.sql
-- Add profile related columns to users table

ALTER TABLE users 
ADD COLUMN first_name VARCHAR(100),
ADD COLUMN last_name VARCHAR(100),
ADD COLUMN profile_image_url VARCHAR(255);
