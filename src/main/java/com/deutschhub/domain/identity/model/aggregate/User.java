package com.deutschhub.domain.identity.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.enumtype.RoleType;
import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.domain.identity.model.valueobject.Password;
import com.deutschhub.domain.identity.model.valueobject.Username;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User implements Auditable {

    private final UUID id;

    private Username username;
    private Email email;
    private Password password;
    private FullName fullName;
    private String phoneNumber;

    private boolean isActive;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    private final Set<RoleType> roles = new HashSet<>();

    private User(
            Username username,
            Email email,
            Password password,
            FullName fullName,
            String phoneNumber
    ) {

        if (username == null) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME);
        }

        if (email == null) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL);
        }

        if (password == null) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (fullName == null) {
            throw new BusinessException(ErrorCode.INVALID_FULL_NAME);
        }

        this.id = UUID.randomUUID();

        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;

        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;

        this.isActive = true;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        this.lastLoginAt = null;
    }

    public static User register(Username username, Email email, Password password,
                                FullName fullName, String phoneNumber) {

        User user = new User(username, email, password, fullName, phoneNumber);
        user.addRole(RoleType.USER);
        return user;
    }

    public void addRole(RoleType role) {
        if (role == null) {
            throw new BusinessException(ErrorCode.INVALID_ROLE_NAME);
        }
        if (roles.contains(role)) {
            return;
        }
        roles.add(role);
        touch();
    }

    public void removeRole(RoleType role) {
        if (role == null) {
            throw new BusinessException(ErrorCode.INVALID_ROLE_NAME);
        }
        if (!roles.contains(role)) {
            return;
        }
        if (roles.size() == 1) {
            throw new BusinessException(ErrorCode.USER_MUST_HAVE_AT_LEAST_ONE_ROLE);
        }
        roles.remove(role);
        touch();
    }

    public void activate() {
        if (isActive) {
            throw new BusinessException(ErrorCode.USER_ALREADY_ACTIVE);
        }
        this.isActive = true;
        touch();
    }

    public void deactivate() {
        if (!isActive) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DEACTIVATED);
        }
        this.isActive = false;
        touch();
    }

    public void validateCanLogin() {
        if (!isActive) {
            throw new BusinessException(ErrorCode.USER_DEACTIVATED);
        }
    }

    public void updateLastLogin() {
        validateCanLogin();
        this.lastLoginAt = LocalDateTime.now();
        touch();
    }

    public void changePassword(Password newPassword) {
        if (newPassword == null) {throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        if (this.password.equals(newPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_ALREADY_USED);
        }
        this.password = newPassword;
        touch();
    }

    public void changeEmail(Email newEmail) {
        if (newEmail == null) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL);
        }
        if (this.email.equals(newEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_USED);
        }
        this.email = newEmail;
        touch();
    }

    public void changeFullName(FullName newFullName) {
        if (newFullName == null) {
            throw new BusinessException(ErrorCode.INVALID_FULL_NAME);
        }
        this.fullName = newFullName;
        touch();
    }

    public void changePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber != null ? newPhoneNumber.trim() : null;
        touch();
    }

    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public FullName getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isActive() {
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

    public Set<RoleType> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}