package com.skillmentor.repository;
import com.skillmentor.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
//* Find all subjects taught by a mentor
    List<Subject> findByMentorId(Long mentorId);


    // * Search subjects by name
    @Query("SELECT s FROM Subject s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Subject> searchByName(String searchTerm);

    /**
     * Count subjects taught by mentor

     */
    long countByMentorId(Long mentorId);
}
