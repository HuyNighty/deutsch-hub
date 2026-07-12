package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.AdminEnrollmentDetailResponse;
import com.deutschhub.application.learning.dto.response.CompletedLessonDetailResponse;
import com.deutschhub.application.learning.port.in.ExpireEnrollmentUseCase;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.application.learning.port.out.LessonCompletionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.entity.LessonCompletion;
import com.deutschhub.domain.learning.model.valueobject.Progress;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExpireEnrollmentService implements ExpireEnrollmentUseCase {

    EnrollmentRepositoryPort enrollmentRepositoryPort;
    LessonCompletionRepositoryPort lessonCompletionRepositoryPort;

    @Override
    public AdminEnrollmentDetailResponse expireEnrollment(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepositoryPort.findById(enrollmentId).orElseThrow(
                () -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        enrollment.expire();

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        List<CompletedLessonDetailResponse> completedLessonDetail = lessonCompletionRepositoryPort
                .findByEnrollmentId(savedEnrollment.getId()).stream()
                .sorted(Comparator.comparing(LessonCompletion::getCompletedAt).reversed())
                .map(this::toCompletedLessonDetailResponse)
                .toList();

        return toResponse(savedEnrollment, completedLessonDetail);
    }

    private AdminEnrollmentDetailResponse toResponse(
            Enrollment enrollment,
            List<CompletedLessonDetailResponse> completedLessonDetails
    ) {
        Progress progress = enrollment.getProgress();

        return new AdminEnrollmentDetailResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                enrollment.getStatus().name(),
                progress.getCompletedLessons(),
                progress.getTotalLessons(),
                progress.getCompletionPercentage(),
                progress.getTotalStudyMinutes(),
                completedLessonDetails,
                enrollment.getEnrolledAt(),
                enrollment.getCompletedAt(),
                enrollment.getDroppedAt(),
                enrollment.getExpiredAt(),
                progress.getLastUpdatedAt()
        );
    }

    private CompletedLessonDetailResponse toCompletedLessonDetailResponse(
            LessonCompletion lessonCompletion
    ) {
        return new CompletedLessonDetailResponse(
                lessonCompletion.getLessonId(),
                lessonCompletion.getCompletedAt()
        );
    }
}
