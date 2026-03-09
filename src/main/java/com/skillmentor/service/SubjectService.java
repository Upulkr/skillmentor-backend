package com.skillmentor.service;

import com.skillmentor.dto.request.CreateSubjectRequest;
import com.skillmentor.dto.response.SubjectResponse;
import com.skillmentor.entity.Mentor;
import com.skillmentor.entity.Subject;
import com.skillmentor.exception.ResourceNotFoundException;
import com.skillmentor.repository.MentorRepository;
import com.skillmentor.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final MentorRepository mentorRepository;

    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public SubjectResponse getSubjectById(Long id) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return convertToResponse(subject);
    }

    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        Long mid = request.getMentorId();
        if (mid == null)
            throw new IllegalArgumentException("Mentor ID cannot be null");
        Mentor mentor = mentorRepository.findById(mid)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + mid));

        Subject subject = Subject.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .mentor(mentor)
                .build();

        Subject savedSubject = subjectRepository.save(subject);
        return convertToResponse(savedSubject);
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, com.skillmentor.dto.request.UpdateSubjectRequest request) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (request.getName() != null)
            subject.setName(request.getName());
        if (request.getDescription() != null)
            subject.setDescription(request.getDescription());
        if (request.getImageUrl() != null)
            subject.setImageUrl(request.getImageUrl());

        Long mid = request.getMentorId();
        if (mid != null) {
            Mentor mentor = mentorRepository.findById(mid)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Mentor not found with id: " + mid));
            subject.setMentor(mentor);
        }

        Subject savedSubject = subjectRepository.save(subject);
        return convertToResponse(savedSubject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject not found with id: " + id);
        }
        subjectRepository.deleteById(id);
    }

    private SubjectResponse convertToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .mentorId(subject.getMentor().getId())
                .mentorName(subject.getMentor().getFirstName() + " " + subject.getMentor().getLastName())
                .name(subject.getName())
                .description(subject.getDescription())
                .imageUrl(subject.getImageUrl())
                .enrollmentCount(subject.getSessions().size()) // Simple count for now
                .createdAt(subject.getCreatedAt())
                .build();
    }
}
