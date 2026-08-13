package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.dto.response.CompletedLessonsResponse;
import com.deutschhub.application.learning.port.in.GetCompletedLessonsUseCase;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.application.learning.port.out.LessonCompletionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.entity.LessonCompletion;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetCompletedLessonsService implements GetCompletedLessonsUseCase {

    LessonCompletionRepositoryPort lessonCompletionRepositoryPort;
    EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    public CompletedLessonsResponse getCompletedLessons(UUID userId, UUID courseId) {
        Enrollment enrollment = enrollmentRepositoryPort.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        List<UUID> completedLessons = lessonCompletionRepositoryPort.findByEnrollmentId(enrollment.getId())
                .stream()
                .map(LessonCompletion::getLessonId)
                .toList();

        return new CompletedLessonsResponse(courseId, completedLessons);
    }
}
