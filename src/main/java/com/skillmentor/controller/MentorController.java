package com.skillmentor.controller;

import com.skillmentor.dto.request.CreateMentorRequest;
import com.skillmentor.dto.response.MentorResponse;
import com.skillmentor.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    /**
     * PUBLIC: List all mentors
     */
    @GetMapping
    public List<MentorResponse> getAllMentors() {
        return mentorService.getAllMentors();
    }

    /**
     * PUBLIC: Get mentor profile
     */
    @GetMapping("/{id}")
    public MentorResponse getMentorProfile(@PathVariable Long id) {
        return mentorService.getMentorProfile(id);
    }

    /**
     * ADMIN: Create new mentor
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MentorResponse> createMentor(@RequestBody @Valid CreateMentorRequest request) {
        MentorResponse response = mentorService.createMentor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ADMIN: Update mentor
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MentorResponse> updateMentor(@PathVariable Long id,
            @RequestBody com.skillmentor.dto.request.UpdateMentorRequest request) {
        MentorResponse response = mentorService.updateMentor(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * ADMIN: Delete mentor
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMentor(@PathVariable Long id) {
        mentorService.deleteMentor(id);
        return ResponseEntity.noContent().build();
    }
}
