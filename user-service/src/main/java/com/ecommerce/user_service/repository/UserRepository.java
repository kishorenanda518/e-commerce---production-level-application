package com.ecommerce.user_service.repository;


import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // ── Finders ──────────────────────────────────────────────────────
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    // ── Existence Checks ─────────────────────────────────────────────
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // ── Status Queries ───────────────────────────────────────────────
    List<User> findByStatus(UserStatus status);

    // ── Custom JPQL ──────────────────────────────────────────────────
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.emailVerified = true")
    Optional<User> findVerifiedUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.createdAt < :date")
    List<User> findByStatusAndCreatedAtBefore(
            @Param("status") UserStatus status,
            @Param("date") Instant date);

    // ── Update Queries ───────────────────────────────────────────────
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.status = 'ACTIVE' WHERE u.email = :email")
    int verifyEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :id")
    int updateLastLoginAt(@Param("id") String id, @Param("lastLoginAt") Instant lastLoginAt);
}