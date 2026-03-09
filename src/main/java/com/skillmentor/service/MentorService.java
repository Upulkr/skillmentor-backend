package com.skillmentor.service;

import com.skillmentor.dto.request.CreateMentorRequest;
import com.skillmentor.dto.response.MentorResponse;
import com.skillmentor.dto.response.SubjectResponse;
import com.skillmentor.entity.Mentor;
import com.skillmentor.entity.User;
import com.skillmentor.exception.ResourceNotFoundException;
import com.skillmentor.repository.MentorRepository;
import com.skillmentor.repository.ReviewRepository;
import com.skillmentor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MENTOR SERVICE
 * 
 * Handles all business logic related to Mentors.
 */
@Service
@RequiredArgsConstructor
public class MentorService {

        private final MentorRepository mentorRepository;
        private final UserRepository userRepository;
        private final ReviewRepository reviewRepository;

        /**
         * GET ALL MENTORS (Public)
         */
        @Transactional(readOnly = true)
        public List<MentorResponse> getAllMentors() {
                return mentorRepository.findAll().stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * GET SINGLE MENTOR PROFILE
         */
        @Transactional(readOnly = true)
        public MentorResponse getMentorProfile(Long id) {
                Mentor mentor = mentorRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
                return convertToResponse(mentor);
        }

        /**
         * CREATE MENTOR (Admin)
         * 
         * In a real scenario, we might link this to an existing User
         * or create a User first. For now, we'll assume the user email
         * exists or we create a dummy user.
         */
        @Transactional
        public MentorResponse createMentor(CreateMentorRequest request) {
                // 1. Find or create user
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseGet(() -> {
                                        User newUser = User.builder()
                                                        .email(request.getEmail())
                                                        .clerkId("clerk_" + System.currentTimeMillis()) // Dummy clerkId
                                                        .role(com.skillmentor.entity.UserRole.MENTOR)
                                                        .firstName(request.getFirstName())
                                                        .lastName(request.getLastName())
                                                        .profileImageUrl(request.getProfileImageUrl())
                                                        .build();
                                        return userRepository.save(newUser);
                                });

                // Ensure user has correct role and name if they already existed as a student
                user.setRole(com.skillmentor.entity.UserRole.MENTOR);
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setProfileImageUrl(request.getProfileImageUrl());
                userRepository.save(user);

                // 2. Map DTO to Entity
                Mentor mentor = Mentor.builder()
                                .user(user)
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .phone(request.getPhone())
                                .title(request.getTitle())
                                .profession(request.getProfession())
                                .company(request.getCompany())
                                .experienceYears(request.getExperienceYears())
                                .bio(request.getBio())
                                .profileImageUrl(request.getProfileImageUrl())
                                .isCertified(request.getIsCertified())
                                .startYear(request.getStartYear())
                                .build();

                // 3. Save
                Mentor savedMentor = mentorRepository.save(mentor);

                return convertToResponse(savedMentor);
        }

        /**
         * UPDATE MENTOR (Admin)
         */
        @Transactional
        public MentorResponse updateMentor(Long id, com.skillmentor.dto.request.UpdateMentorRequest request) {
                Mentor mentor = mentorRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

                User user = mentor.getUser();
                if (user == null) {
                        throw new IllegalStateException("Mentor #" + id + " does not have an associated user account.");
                }

                // 1. Email Check
                if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
                        if (userRepository.existsByEmail(request.getEmail())) {
                                throw new IllegalArgumentException(
                                                "Email '" + request.getEmail() + "' is already in use by another user");
                        }
                        user.setEmail(request.getEmail());
                }

                // 2. Name & Profile Image updates (Sync to User as well)
                if (request.getFirstName() != null) {
                        mentor.setFirstName(request.getFirstName());
                        user.setFirstName(request.getFirstName());
                }
                if (request.getLastName() != null) {
                        mentor.setLastName(request.getLastName());
                        user.setLastName(request.getLastName());
                }
                if (request.getProfileImageUrl() != null) {
                        mentor.setProfileImageUrl(request.getProfileImageUrl());
                        user.setProfileImageUrl(request.getProfileImageUrl());
                }

                // 3. Mentor-specific fields
                if (request.getPhone() != null)
                        mentor.setPhone(request.getPhone());
                if (request.getTitle() != null)
                        mentor.setTitle(request.getTitle());
                if (request.getProfession() != null)
                        mentor.setProfession(request.getProfession());
                if (request.getCompany() != null)
                        mentor.setCompany(request.getCompany());
                if (request.getExperienceYears() != null)
                        mentor.setExperienceYears(request.getExperienceYears());
                if (request.getBio() != null)
                        mentor.setBio(request.getBio());
                if (request.getIsCertified() != null)
                        mentor.setIsCertified(request.getIsCertified());
                if (request.getStartYear() != null)
                        mentor.setStartYear(request.getStartYear());

                userRepository.save(user);
                Mentor savedMentor = mentorRepository.save(mentor);
                return convertToResponse(savedMentor);
        }

        /**
         * DELETE MENTOR (Admin)
         */
        @Transactional
        public void deleteMentor(Long id) {
                if (!mentorRepository.existsById(id)) {
                        throw new ResourceNotFoundException("Mentor not found with id: " + id);
                }
                mentorRepository.deleteById(id);
        }

        /**
         * HELPER: Map Entity to Response DTO
         */
        private MentorResponse convertToResponse(Mentor mentor) {
                Double avgRating = reviewRepository.getAverageRatingByMentorId(mentor.getId());
                long reviewCount = reviewRepository.countByMentorId(mentor.getId());

                return MentorResponse.builder()
                                .id(mentor.getId())
                                .firstName(mentor.getFirstName())
                                .lastName(mentor.getLastName())
                                .title(mentor.getTitle())
                                .profession(mentor.getProfession())
                                .company(mentor.getCompany())
                                .experienceYears(mentor.getExperienceYears())
                                .bio(mentor.getBio())
                                .profileImageUrl(mentor.getProfileImageUrl())
                                .isCertified(mentor.getIsCertified())
                                .startYear(mentor.getStartYear())
                                .averageRating(avgRating != null ? avgRating : 0.0)
                                .reviewCount((int) reviewCount)
                                .createdAt(mentor.getCreatedAt())
                                .subjects(mentor.getSubjects().stream()
                                                .map(s -> SubjectResponse.builder()
                                                                .id(s.getId())
                                                                .name(s.getName())
                                                                .description(s.getDescription())
                                                                .imageUrl(s.getImageUrl())
                                                                .enrollmentCount(s.getSessions().size())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }
}
