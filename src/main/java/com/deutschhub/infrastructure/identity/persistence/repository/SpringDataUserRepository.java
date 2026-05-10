package com.deutschhub.infrastructure.identity.persistence.repository;

import com.deutschhub.infrastructure.identity.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
