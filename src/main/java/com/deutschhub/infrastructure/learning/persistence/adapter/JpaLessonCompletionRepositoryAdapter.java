package com.deutschhub.infrastructure.learning.persistence.adapter;

import com.deutschhub.application.learning.port.out.LessonCompletionRepositoryPort;
import com.deutschhub.domain.learning.model.entity.LessonCompletion;
import com.deutschhub.infrastructure.learning.persistence.entity.LessonCompletionJpaEntity;
import com.deutschhub.infrastructure.learning.persistence.repository.SpringDataLessonCompletionRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaLessonCompletionRepositoryAdapter implements LessonCompletionRepositoryPort {

    SpringDataLessonCompletionRepository repository;

    @Override
    public LessonCompletion save(LessonCompletion lessonCompletion) {
        LessonCompletionJpaEntity saved = repository.save(toEntity(lessonCompletion));

        return toDomain(saved);
    }

    @Override
    public boolean existsByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId) {
        return repository.existsByEnrollmentIdAndLessonId(enrollmentId, lessonId);
    }

    @Override
    public long countByEnrollmentId(UUID enrollmentId) {
        return repository.countByEnrollmentId(enrollmentId);
    }

    private LessonCompletionJpaEntity toEntity(LessonCompletion lessonCompletion) {
        return LessonCompletionJpaEntity.builder()
                .id(lessonCompletion.getId())
                .enrollmentId(lessonCompletion.getEnrollmentId())
                .lessonId(lessonCompletion.getLessonId())
                .completedAt(lessonCompletion.getCompletionAt())
                .build();
    }

    private LessonCompletion toDomain(LessonCompletionJpaEntity entity) {
        return LessonCompletion.restore(entity.getId(), entity.getEnrollmentId(),
                entity.getLessonId(), entity.getCompletedAt());
    }
}
