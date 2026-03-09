-- V5__Add_Payment_Slip_To_Sessions.sql
-- Add payment_slip_url column to sessions table
ALTER TABLE sessions ADD COLUMN payment_slip_url VARCHAR(500);
