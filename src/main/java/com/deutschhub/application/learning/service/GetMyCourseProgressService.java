package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.dto.response.EnrollmentProgressResponse;
import com.deutschhub.application.learning.port.in.GetMyCourseProgressUseCase;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.valueobject.Progress;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMyCourseProgressService implements GetMyCourseProgressUseCase {

    EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    public EnrollmentProgressResponse getProgress(UUID userId, UUID courseId) {
        Enrollment enrollment = enrollmentRepositoryPort.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        return toResponse(enrollment);
    }

    private EnrollmentProgressResponse toResponse(Enrollment enrollment) {
        Progress progress = enrollment.getProgress();

        return new EnrollmentProgressResponse(
                enrollment.getId(),
                enrollment.getCourseId(),
                enrollment.getStatus().name(),
                progress.getCompletedLessons(),
                progress.getTotalLessons(),
                progress.getCompletionPercentage(),
                progress.getTotalStudyMinutes(),
                progress.getLastUpdatedAt()
        );
    }
}