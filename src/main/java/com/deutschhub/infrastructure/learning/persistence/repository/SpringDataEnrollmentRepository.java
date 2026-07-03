package com.deutschhub.infrastructure.learning.persistence.repository;

import com.deutschhub.infrastructure.learning.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, UUID> {

    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    Optional<EnrollmentJpaEntity> findByUserIdAndCourseId(UUID userId, UUID courseId);

    List<EnrollmentJpaEntity> findByUserId(UUID userId);
}
