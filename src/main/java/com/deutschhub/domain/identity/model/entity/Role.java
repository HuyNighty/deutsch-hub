package com.deutschhub.domain.identity.model.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Role {

    private final UUID id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions = new HashSet<>();

    public Role(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name.toUpperCase().trim();
        this.description = description;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(permissionName));
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}
