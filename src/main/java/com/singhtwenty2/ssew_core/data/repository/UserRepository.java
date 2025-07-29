package com.singhtwenty2.ssew_core.data.repository;

import com.singhtwenty2.ssew_core.data.entity.User;
import com.singhtwenty2.ssew_core.data.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMobileNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String phoneNumber);

    Page<User> findByRole(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isAccountLocked = true")
    Page<User> findLockedAccounts(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.lastLoginTime < :cutoffDate OR u.lastLoginTime IS NULL")
    Page<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.isAccountLocked = false, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.lastLoginTime < :cutoffDate")
    int deactivateInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}