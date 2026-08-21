package com.deutschhub.infrastructure.identity.persistence.repository;

import com.deutschhub.domain.identity.enums.RoleType;
import com.deutschhub.infrastructure.identity.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByEmail(String email);

    @Query("""
        SELECT u
        FROM UserJpaEntity u
        WHERE :keyword IS NULL
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    )
    Page<UserJpaEntity> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
           SELECT COUNT(u) > 0
           FROM UserJpaEntity u
           JOIN u.roles r
           WHERE u.id = :userId
                AND u.isActive = true
                AND r = :role
    """)
    boolean existsActiveContentEditor(@Param("userId") UUID userId, @Param("role") RoleType role);
}
