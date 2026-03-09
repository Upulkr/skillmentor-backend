package com.skillmentor.entity;

public enum PaymentStatus {
    PENDING, // Waiting for payment proof
    UNDER_REVIEW, // Student uploaded slip, admin must verify
    CONFIRMED // Payment approved
}
