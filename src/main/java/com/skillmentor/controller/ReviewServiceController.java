package com.skillmentor.controller;

import com.skillmentor.dto.request.SubmitReviewRequest;
import com.skillmentor.dto.response.ReviewResponse;
import com.skillmentor.entity.User;
import com.skillmentor.exception.ResourceNotFoundException;
import com.skillmentor.repository.UserRepository;
import com.skillmentor.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewServiceController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(
            @RequestBody @Valid SubmitReviewRequest request,
            @AuthenticationPrincipal String clerkId) {

        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ReviewResponse response = reviewService.submitReview(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mentor/{mentorId}")
    public List<ReviewResponse> getMentorReviews(@PathVariable Long mentorId) {
        return reviewService.getMentorReviews(mentorId);
    }
}
