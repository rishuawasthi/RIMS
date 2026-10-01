package com.hcl.inventory.service;

import com.hcl.inventory.entity.AppUser;
import com.hcl.inventory.enums.UserRole;
import com.hcl.inventory.enums.UserStatus;
import com.hcl.inventory.exception.DuplicateUserEmailException;
import com.hcl.inventory.exception.DuplicateUsernameException;
import com.hcl.inventory.exception.InvalidUserStateException;
import com.hcl.inventory.exception.UserNotFoundException;
import com.hcl.inventory.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {

        return appUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser getUserById(
            Integer userId
    ) {

        return appUserRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id "
                                        + userId
                                        + " not found"
                        )
                );
    }

    @Transactional
    public AppUser addUser(
            AppUser user
    ) {

        String username =
                normalizeUsername(
                        user.getUsername()
                );

        String email =
                normalizeEmail(
                        user.getEmail()
                );

        checkDuplicateUsername(username);

        checkDuplicateEmail(email);

        user.setUsername(username);
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        if (user.getRole() == null) {

            user.setRole(
                    UserRole.STORE_STAFF
            );
        }

        if (user.getStatus() == null) {

            user.setStatus(
                    UserStatus.ACTIVE
            );
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(null);

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser updateUser(
            Integer userId,
            AppUser updatedUser
    ) {

        AppUser existingUser =
                getUserById(userId);

        String username =
                normalizeUsername(
                        updatedUser.getUsername()
                );

        String email =
                normalizeEmail(
                        updatedUser.getEmail()
                );

        if (appUserRepository
                .existsByUsernameIgnoreCaseAndUserIdNot(
                        username,
                        userId
                )) {

            throw new DuplicateUsernameException(
                    "Username "
                            + username
                            + " is already in use"
            );
        }

        if (appUserRepository
                .existsByEmailIgnoreCaseAndUserIdNot(
                        email,
                        userId
                )) {

            throw new DuplicateUserEmailException(
                    "Email "
                            + email
                            + " is already in use"
            );
        }

        existingUser.setUsername(username);

        existingUser.setFullName(
                updatedUser.getFullName()
        );

        existingUser.setEmail(email);

        existingUser.setPhone(
                updatedUser.getPhone()
        );

        if (updatedUser.getRole() != null) {

            existingUser.setRole(
                    updatedUser.getRole()
            );
        }

        if (updatedUser.getStatus() != null) {

            existingUser.setStatus(
                    updatedUser.getStatus()
            );
        }

        /*
         * Password is updated only when a new password
         * is supplied.
         */
        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            existingUser.setPassword(
                    passwordEncoder.encode(
                            updatedUser.getPassword()
                    )
            );
        }

        return appUserRepository.save(
                existingUser
        );
    }

    @Transactional
    public AppUser changePassword(
            Integer userId,
            String newPassword
    ) {

        AppUser user =
                getUserById(userId);

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        if (user.getStatus()
                == UserStatus.LOCKED) {

            user.setStatus(
                    UserStatus.ACTIVE
            );
        }

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser activateUser(
            Integer userId
    ) {

        AppUser user =
                getUserById(userId);

        user.setStatus(
                UserStatus.ACTIVE
        );

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser deactivateUser(
            Integer userId
    ) {

        AppUser user =
                getUserById(userId);

        user.setStatus(
                UserStatus.INACTIVE
        );

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser lockUser(
            Integer userId
    ) {

        AppUser user =
                getUserById(userId);

        user.setStatus(
                UserStatus.LOCKED
        );

        user.setLockedUntil(
                LocalDateTime.now().plusMinutes(30)
        );

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser unlockUser(
            Integer userId
    ) {

        AppUser user =
                getUserById(userId);

        if (user.getStatus()
                != UserStatus.LOCKED) {

            throw new InvalidUserStateException(
                    "User is not locked"
            );
        }

        user.setStatus(
                UserStatus.ACTIVE
        );

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        return appUserRepository.save(user);
    }

    @Transactional
    public void recordFailedLogin(
            Integer userId
    ) {

        AppUser user =
                getUserById(userId);

        int attempts =
                user.getFailedLoginAttempts() == null
                        ? 0
                        : user.getFailedLoginAttempts();

        attempts++;

        user.setFailedLoginAttempts(
                attempts
        );

        /*
         * Lock after 5 failed attempts.
         * Actual authentication handling will be
         * connected during Spring Security implementation.
         */
        if (attempts >= 5) {

            user.setStatus(
                    UserStatus.LOCKED
            );

            user.setLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(30)
            );
        }

        appUserRepository.save(user);
    }

    @Transactional
    public void recordSuccessfulLogin(
            Integer userId
    ) {

        AppUser user =
                getUserById(userId);

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(
                LocalDateTime.now()
        );

        if (user.getStatus()
                == UserStatus.LOCKED) {

            user.setStatus(
                    UserStatus.ACTIVE
            );
        }

        appUserRepository.save(user);
    }

    @Transactional
    public void deleteUser(
            Integer userId
    ) {

        AppUser user =
                getUserById(userId);

        appUserRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public AppUser getByUsername(
            String username
    ) {

        String normalizedUsername =
                normalizeUsername(username);

        return appUserRepository
                .findByUsernameIgnoreCase(
                        normalizedUsername
                )
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with username "
                                        + normalizedUsername
                                        + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public AppUser getByEmail(
            String email
    ) {

        String normalizedEmail =
                normalizeEmail(email);

        return appUserRepository
                .findByEmailIgnoreCase(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email "
                                        + normalizedEmail
                                        + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<AppUser> searchByName(
            String name
    ) {

        return appUserRepository
                .findByFullNameContainingIgnoreCase(
                        name
                );
    }

    @Transactional(readOnly = true)
    public List<AppUser> getByRole(
            UserRole role
    ) {

        return appUserRepository.findByRole(
                role
        );
    }

    @Transactional(readOnly = true)
    public List<AppUser> getByStatus(
            UserStatus status
    ) {

        return appUserRepository.findByStatus(
                status
        );
    }

    private void checkDuplicateUsername(
            String username
    ) {

        if (appUserRepository
                .existsByUsernameIgnoreCase(
                        username
                )) {

            throw new DuplicateUsernameException(
                    "Username "
                            + username
                            + " is already in use"
            );
        }
    }

    private void checkDuplicateEmail(
            String email
    ) {

        if (appUserRepository
                .existsByEmailIgnoreCase(
                        email
                )) {

            throw new DuplicateUserEmailException(
                    "Email "
                            + email
                            + " is already in use"
            );
        }
    }

    private String normalizeUsername(
            String username
    ) {

        if (username == null) {
            return null;
        }

        return username
                .trim()
                .toLowerCase();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }
}