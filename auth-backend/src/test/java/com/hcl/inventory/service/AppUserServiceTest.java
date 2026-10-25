package com.hcl.inventory.service;

import com.hcl.inventory.entity.AppUser;
import com.hcl.inventory.enums.UserRole;
import com.hcl.inventory.enums.UserStatus;
import com.hcl.inventory.exception.DuplicateUserEmailException;
import com.hcl.inventory.exception.DuplicateUsernameException;
import com.hcl.inventory.exception.UserNotFoundException;
import com.hcl.inventory.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AppUserService}.
 */
@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService userService;

    private AppUser sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new AppUser();
        sampleUser.setUserId(1);
        sampleUser.setUsername("john.doe");
        sampleUser.setEmail("john.doe@example.com");
        sampleUser.setFullName("John Doe");
        sampleUser.setPhone("9876543210");
        sampleUser.setPassword("hashed_password");
        sampleUser.setRole(UserRole.STORE_STAFF);
        sampleUser.setStatus(UserStatus.ACTIVE);
        sampleUser.setFailedLoginAttempts(0);
    }

    // ---------------------------------------------------------------
    // getUserById
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getUserById returns user when id exists")
    void getUserById_found() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

        AppUser result = userService.getUserById(1);

        assertThat(result.getUsername()).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("getUserById throws UserNotFoundException when id missing")
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // addUser
    // ---------------------------------------------------------------

    @Test
    @DisplayName("addUser hashes password and saves user")
    void addUser_success() {
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(sampleUser);

        AppUser result = userService.addUser(sampleUser);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode(any());
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("addUser throws DuplicateUsernameException when username taken")
    void addUser_duplicateUsername_throwsException() {
        when(userRepository.existsByUsernameIgnoreCase("john.doe")).thenReturn(true);

        assertThatThrownBy(() -> userService.addUser(sampleUser))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addUser throws DuplicateUserEmailException when email taken")
    void addUser_duplicateEmail_throwsException() {
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("john.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.addUser(sampleUser))
                .isInstanceOf(DuplicateUserEmailException.class);
    }

    // ---------------------------------------------------------------
    // activateUser / deactivateUser / lockUser / unlockUser
    // ---------------------------------------------------------------

    @Test
    @DisplayName("activateUser sets status to ACTIVE")
    void activateUser_setsActiveStatus() {
        sampleUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = userService.activateUser(1);

        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("deactivateUser sets status to INACTIVE")
    void deactivateUser_setsInactiveStatus() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = userService.deactivateUser(1);

        assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("lockUser sets status to LOCKED and sets lockedUntil")
    void lockUser_setsLockedStatus() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = userService.lockUser(1);

        assertThat(result.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(result.getLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("changePassword encodes and saves new password")
    void changePassword_encodesNewPassword() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("hashed_new");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword(1, "newpassword");

        assertThat(sampleUser.getPassword()).isEqualTo("hashed_new");
    }

    // ---------------------------------------------------------------
    // getByUsername
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getByUsername returns user when username exists")
    void getByUsername_found() {
        when(userRepository.findByUsernameIgnoreCase("john.doe"))
                .thenReturn(Optional.of(sampleUser));

        AppUser result = userService.getByUsername("john.doe");

        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("getByUsername throws UserNotFoundException when username missing")
    void getByUsername_notFound_throwsException() {
        when(userRepository.findByUsernameIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByUsername("ghost"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
