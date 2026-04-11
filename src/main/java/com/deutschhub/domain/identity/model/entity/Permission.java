package com.deutschhub.domain.identity.model.entity;

import java.util.UUID;

public class Permission {

    private final UUID id;
    private final String name;
    private final String description;

    public Permission(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name.toUpperCase().trim();
        this.description = description;
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
}
