package com.hcl.inventory.controller;

import com.hcl.inventory.entity.AppUser;
import com.hcl.inventory.enums.UserRole;
import com.hcl.inventory.enums.UserStatus;
import com.hcl.inventory.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService appUserService;

    @Autowired
    public AppUserController(
            AppUserService appUserService
    ) {

        this.appUserService = appUserService;
    }

    @GetMapping
    public ResponseEntity<List<AppUser>>
    getAllUsers() {

        return ResponseEntity.ok(
                appUserService.getAllUsers()
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AppUser>
    getUserById(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                appUserService.getUserById(
                        userId
                )
        );
    }

    @PostMapping
    public ResponseEntity<AppUser>
    addUser(
            @Valid @RequestBody AppUser user
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        appUserService.addUser(user)
                );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AppUser>
    updateUser(
            @PathVariable Integer userId,
            @RequestBody AppUser user
    ) {

        return ResponseEntity.ok(
                appUserService.updateUser(
                        userId,
                        user
                )
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void>
    deleteUser(
            @PathVariable Integer userId
    ) {

        appUserService.deleteUser(userId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<AppUser>
    getByUsername(
            @PathVariable String username
    ) {

        return ResponseEntity.ok(
                appUserService.getByUsername(
                        username
                )
        );
    }

    @GetMapping("/email")
    public ResponseEntity<AppUser>
    getByEmail(
            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                appUserService.getByEmail(email)
        );
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<AppUser>>
    searchByName(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                appUserService.searchByName(name)
        );
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<AppUser>>
    getByRole(
            @PathVariable UserRole role
    ) {

        return ResponseEntity.ok(
                appUserService.getByRole(role)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppUser>>
    getByStatus(
            @PathVariable UserStatus status
    ) {

        return ResponseEntity.ok(
                appUserService.getByStatus(status)
        );
    }

    @PostMapping("/{userId}/activate")
    public ResponseEntity<AppUser>
    activateUser(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                appUserService.activateUser(
                        userId
                )
        );
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<AppUser>
    deactivateUser(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                appUserService.deactivateUser(
                        userId
                )
        );
    }

    @PostMapping("/{userId}/lock")
    public ResponseEntity<AppUser>
    lockUser(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                appUserService.lockUser(
                        userId
                )
        );
    }

    @PostMapping("/{userId}/unlock")
    public ResponseEntity<AppUser>
    unlockUser(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                appUserService.unlockUser(
                        userId
                )
        );
    }

    @PostMapping("/{userId}/change-password")
    public ResponseEntity<Map<String, String>>
    changePassword(
            @PathVariable Integer userId,
            @RequestParam String newPassword
    ) {

        appUserService.changePassword(
                userId,
                newPassword
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password changed successfully"
                )
        );
    }
}