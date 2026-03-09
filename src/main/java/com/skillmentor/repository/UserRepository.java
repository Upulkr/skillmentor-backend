package com.skillmentor.repository;

import com.skillmentor.entity.User;
import com.skillmentor.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByClerkId(String clerkId);

    boolean existsByEmail(String email);

    boolean existsByClerkId(String clerkId);

    List<User> findAllByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE u.role = ?1 AND u.email = ?2")
    Optional<User> findAdminWithEmail(UserRole role, String email);

    long countByRole(UserRole role);
}
