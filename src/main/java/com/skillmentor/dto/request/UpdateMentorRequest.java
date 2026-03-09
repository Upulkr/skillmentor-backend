package com.skillmentor.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMentorRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String title;
    private String profession;
    private String company;
    private Integer experienceYears;
    private String bio;
    private String profileImageUrl;
    private Boolean isCertified;
    private Integer startYear;
}
