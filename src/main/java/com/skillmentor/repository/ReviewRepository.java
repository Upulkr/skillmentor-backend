package com.skillmentor.repository;

import com.skillmentor.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMentorId(Long mentorId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mentor.id = ?1")
    Double getAverageRatingByMentorId(Long mentorId);

    long countByMentorId(Long mentorId);
}
