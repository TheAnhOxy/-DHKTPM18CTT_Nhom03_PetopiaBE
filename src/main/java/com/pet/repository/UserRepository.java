package com.pet.repository;

import com.pet.entity.User;
import com.pet.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findById(String id);


    @Query("""
SELECT u FROM User u
WHERE u.username = :identifier
   OR u.email = :identifier
""")
    Optional<User> findByIdentifier(String identifier);


    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u.userId FROM User u ORDER BY u.userId DESC LIMIT 1")
    Optional<String> findLastUserId();

    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameContainingIgnoreCase(String userName);
    long countByRoleAndCreatedAtBetween(com.pet.enums.UserRole role, LocalDateTime start, LocalDateTime end);
    boolean existsByPhoneNumber(String phoneNumber);

    @Query("""
    SELECT u FROM User u
    WHERE (:keyword IS NULL 
        OR LOWER(u.fullName) LIKE LOWER(:keyword)
        OR LOWER(u.email) LIKE LOWER(:keyword)
        OR LOWER(u.username) LIKE LOWER(:keyword)
        OR LOWER(u.phoneNumber) LIKE LOWER(:keyword))
      AND (:role IS NULL OR u.role = :role)
      AND (:isActive IS NULL OR u.isActive = :isActive)
    """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") UserRole role,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
