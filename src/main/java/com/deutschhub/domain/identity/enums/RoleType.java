package com.deutschhub.domain.identity.enums;

public enum RoleType {
    USER("USER"),
    ADMIN("ADMIN"),
    CONTENT_EDITOR("CONTENT_EDITOR");

    private final String roleName;

    RoleType(String roleName) {
        this.roleName = roleName;
    }
}