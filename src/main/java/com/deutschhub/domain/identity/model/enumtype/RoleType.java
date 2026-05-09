package com.deutschhub.domain.identity.model.enumtype;

public enum RoleType {
    USER("USER"),
    ADMIN("ADMIN");

    private final String roleName;

    RoleType(String roleName) {
        this.roleName = roleName;
    }
}