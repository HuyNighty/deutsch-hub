package com.deutschhub.infrastructure.learning.persistence.adapter;

import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.enums.EnrollmentStatus;
import com.deutschhub.domain.learning.model.valueobject.Progress;
import com.deutschhub.infrastructure.learning.persistence.entity.EnrollmentJpaEntity;
import com.deutschhub.infrastructure.learning.persistence.repository.SpringDataEnrollmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaEnrollmentRepositoryAdapter implements EnrollmentRepositoryPort {

    SpringDataEnrollmentRepository repository;

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity saved = repository.save(toEntity(enrollment));
        return toDomain(saved);
    }

    @Override
    public boolean existsByUserIdAndCourseId(UUID userId, UUID courseId) {
        return repository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId) {
        return repository.findByUserIdAndCourseId(userId, courseId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Enrollment> findById(UUID enrollmentId) {
        return repository.findById(enrollmentId).map(this::toDomain);
    }

    @Override
    public List<Enrollment> findByCourseId(UUID courseId) {
        return repository.findByCourseId(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Enrollment> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private EnrollmentJpaEntity toEntity(Enrollment enrollment) {
        Progress progress = enrollment.getProgress();

        return EnrollmentJpaEntity.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus().name())
                .completedLessons(progress.getCompletedLessons())
                .totalLessons(progress.getTotalLessons())
                .completionPercentage(progress.getCompletionPercentage())
                .totalStudyMinutes(progress.getTotalStudyMinutes())
                .progressLastUpdatedAt(progress.getLastUpdatedAt())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .droppedAt(enrollment.getDroppedAt())
                .expiredAt(enrollment.getExpiredAt())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }

    private Enrollment toDomain(EnrollmentJpaEntity entity) {
        Progress progress = Progress.restore(entity.getCompletedLessons(), entity.getTotalLessons(),
                entity.getTotalStudyMinutes(), entity.getProgressLastUpdatedAt());

        return Enrollment.restore(entity.getId(), entity.getUserId(), entity.getCourseId(), EnrollmentStatus.valueOf(entity.getStatus()),
                progress, entity.getEnrolledAt(), entity.getCompletedAt(), entity.getDroppedAt(), entity.getExpiredAt(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
