package com.skillmentor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mentors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Mentor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User is required")
    private User user;

    @Column(nullable = false)
    @NotBlank(message = "First name is required")
    @Length(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "Last name is required")
    @Length(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    @Length(min = 3, max = 100)
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Profession is required")
    private String profession;

    @Column(nullable = false)
    @NotBlank(message = "Company is required")
    private String company;

    @Column(nullable = false)
    @Min(value = 0, message = "Experience must be at least 0")
    @Max(value = 70, message = "Experience cannot exceed 70 years")
    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    @Length(max = 2000)
    private String bio;

    @Column
    private String profileImageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCertified = false;

    @Column
    private Integer startYear;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Subject> subjects = new HashSet<>();
}
