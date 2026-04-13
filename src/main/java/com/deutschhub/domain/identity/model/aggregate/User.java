package com.deutschhub.domain.identity.model.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.entity.Role;
import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.domain.identity.model.valueobject.Password;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private String username;
    private Password password;
    private Email email;
    private FullName fullName;
    private String phoneNumber;
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    private final Set<Role> roles = new HashSet<>();

    public User(String username, Email email, Password password, FullName fullName, String phoneNumber) {
        this.id = UUID.randomUUID();
        this.username = validateUserName(username);
        this.email = validateEmail(email);
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    public void addRole(Role role) {
        if (role == null)
            throw new BusinessException(ErrorCode.INVALID_ROLE_NAME);
        this.roles.add(role);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastLogin() {
        this.updatedAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasPermission(String permissionName) {
        if (permissionName == null || !isActive)
            return false;
        return roles.stream()
                .anyMatch(r -> r.hasPermission(permissionName));
    }

    private String validateUserName(String username) {
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME);
        }
        return username.trim();
    }

    private Email validateEmail(Email email) {
        if (email == null) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL);
        }
        return email;
    }

    public UUID getUuid() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public FullName getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Boolean getActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}
