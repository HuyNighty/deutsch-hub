package com.deutschhub.infrastructure.identity.persistence.entity;

import com.deutschhub.domain.identity.model.enumtype.RoleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserJpaEntity {

    @Id
    UUID id;
    @Column(nullable = false, unique = true)
    String username;
    @Column(nullable = false, unique = true)
    String email;
    @Column(nullable = false)
    String password;
    @Column(nullable = false)
    String firstName;
    @Column(nullable = false)
    String lastName;
    String phoneNumber;
    boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime lastLoginAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<RoleType> roles = new HashSet<>();
}
