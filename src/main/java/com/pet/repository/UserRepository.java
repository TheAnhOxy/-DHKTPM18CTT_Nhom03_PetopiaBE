package com.pet.repository;

import com.pet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findById(String id);

    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByUsernameOrPhoneNumber(String identifier);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u.userId FROM User u ORDER BY u.userId DESC LIMIT 1")
    Optional<String> findLastUserId();

    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);


    boolean existsByPhoneNumber(String phoneNumber);
}
