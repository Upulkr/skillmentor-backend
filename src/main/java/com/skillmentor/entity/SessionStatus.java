package com.skillmentor.entity;

public enum SessionStatus {
    PENDING,       // Waiting for payment approval
    CONFIRMED,     // Payment approved, ready for session
    COMPLETED,     // Session has been completed
    CANCELLED      // Cancelled by student or admin
}
