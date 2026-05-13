package com.deutschhub.infrastructure.identity.persistence.repository;

import com.deutschhub.infrastructure.identity.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByEmail(String email);
}
