package com.skillmentor.service;

import com.skillmentor.dto.request.SubmitReviewRequest;
import com.skillmentor.dto.response.ReviewResponse;
import com.skillmentor.entity.*;
import com.skillmentor.exception.ResourceNotFoundException;
import com.skillmentor.repository.ReviewRepository;
import com.skillmentor.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;

    @Transactional
    public ReviewResponse submitReview(Long studentId, SubmitReviewRequest request) {
        // 1. Fetch session and verify ownership/status
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("You can only review your own sessions");
        }

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("You can only review completed sessions");
        }

        // 2. Create review
        Review review = Review.builder()
                .session(session)
                .student(session.getStudent())
                .mentor(session.getMentor())
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .build();

        Review savedReview = reviewRepository.save(review);
        return convertToResponse(savedReview);
    }

    public List<ReviewResponse> getMentorReviews(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse convertToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .studentName(review.getStudent().getEmail().split("@")[0]) // Masking email for privacy
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
