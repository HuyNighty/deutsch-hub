package com.deutschhub.domain.identity.model.valueobject;

public class FullName {

    private final String firstName;
    private final String lastName;

    public FullName(String firstName, String lastName) {
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";
    }

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
