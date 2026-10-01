package com.hcl.inventory.repository;

import com.hcl.inventory.entity.AppUser;
import com.hcl.inventory.enums.UserRole;
import com.hcl.inventory.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository
        extends JpaRepository<AppUser, Integer> {

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByUsernameIgnoreCaseAndUserIdNot(
            String username,
            Integer userId
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCaseAndUserIdNot(
            String email,
            Integer userId
    );

    Optional<AppUser> findByUsernameIgnoreCase(
            String username
    );

    Optional<AppUser> findByEmailIgnoreCase(
            String email
    );

    List<AppUser> findByRole(
            UserRole role
    );

    List<AppUser> findByStatus(
            UserStatus status
    );

    List<AppUser> findByFullNameContainingIgnoreCase(
            String fullName
    );
}