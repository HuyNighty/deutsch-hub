package com.deutschhub.infrastructure.learning.persistence.repository;

import com.deutschhub.infrastructure.learning.persistence.entity.LessonCompletionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataLessonCompletionRepository extends JpaRepository<LessonCompletionJpaEntity, UUID> {

    boolean existsByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);

    long countByEnrollmentId(UUID enrollmentId);
}
