package com.skillmentor.repository;

import com.skillmentor.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    //we can find a mentor by their userid
    Optional<Mentor> findByUserId(Long userId);

    //Show only certified mentors
    List<Mentor> findByIsCertifiedTrue();

    //Student can filter: "Show mentors with 5+ years experience"
    List<Mentor> findByExperienceYearsGreaterThanEqual(Integer minYears);


    // Mentors with 5+ years experience AND certified
    @Query("SELECT m FROM Mentor m WHERE " +
            "m.experienceYears >= ?1 " +
            "AND m.isCertified = ?2 " +
            "ORDER BY m.experienceYears DESC")
    List<Mentor> findByCriteria(Integer minExperience, Boolean isCertified);

     /** For admin dashboard: "Total mentors: 42"
            */
    long count();


    // Check if user is a mentor
    boolean existsByUserId(Long userId);
}
