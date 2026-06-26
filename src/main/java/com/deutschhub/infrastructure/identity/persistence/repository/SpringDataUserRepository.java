package com.deutschhub.infrastructure.identity.persistence.repository;

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

    @Query(
            """
                    select u
                    from UserJpaEntity u
                    where :keyword is null
                        or lower(u.username) like lower(concat('%', :keyword, '%'))
                        or lower(u.email) like lower(concat('%', :keyword, '%'))
                        or lower(u.phoneNumber) like lower(concat('%', :keyword, '%'))
                        or lower(u.firstName) like lower(concat('%', :keyword, '%'))
                        or lower(u.lastName) like lower(concat('%', :keyword, '%'))
                    """
    )
    Page<UserJpaEntity> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}
