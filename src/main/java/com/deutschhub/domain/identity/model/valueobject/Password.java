package com.deutschhub.domain.identity.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Password {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private final String hashedValue;

    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    public static Password create(String plainPassword) {
        validatePasswordStrength(plainPassword);
        String hashed = passwordEncoder.encode(plainPassword.trim());
        return new Password(hashed);
    }

    public static Password fromHashed(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return new Password(hashedPassword.trim());
    }

    private static void validatePasswordStrength(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "Password cannot be empty");
        }

        String p = plainPassword.trim();

        if (p.length() < 8) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "Password must be at least 8 characters long");
        }
        if (!p.matches(".*[A-Z].*")) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "Password must contain at least one uppercase letter");
        }
        if (!p.matches(".*[a-z].*")) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "Password must contain at least one lowercase letter");
        }
        if (!p.matches(".*[0-9].*")) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "Password must contain at least one number");
        }
    }

    private static String hashPassword(String plainPassword) {
        return "{bcrypt}" + plainPassword.trim();
    }

    public String getHashedValue() {
        return hashedValue;
    }

    public boolean matches(String plainPassword) {
        if (plainPassword == null) return false;
        return passwordEncoder.matches(hashedValue, plainPassword);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Password other = (Password) obj;
        return hashedValue.equals(other.hashedValue);
    }

    @Override
    public int hashCode() {
        return hashedValue.hashCode();
    }
}