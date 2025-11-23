package com.pet.repository;

import com.pet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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


}
