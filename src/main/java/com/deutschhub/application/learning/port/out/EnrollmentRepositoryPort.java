package com.deutschhub.application.learning.port.out;

import com.deutschhub.domain.learning.model.aggregate.Enrollment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepositoryPort {

    Enrollment save(Enrollment enrollment);

    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);

    List<Enrollment> findByUserId(UUID userId);
}
