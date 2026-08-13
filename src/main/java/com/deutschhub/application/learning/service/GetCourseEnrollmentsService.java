package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.dto.response.AdminCourseEnrollmentResponse;
import com.deutschhub.application.learning.port.in.GetCourseEnrollmentsUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.valueobject.Progress;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetCourseEnrollmentsService implements GetCourseEnrollmentsUseCase {

    CourseRepositoryPort courseRepositoryPort;
    EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    public List<AdminCourseEnrollmentResponse> getCourseEnrollments(UUID courseId) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        return enrollmentRepositoryPort.findByCourseId(courseId)
                .stream()
                .sorted(Comparator.comparing(Enrollment::getEnrolledAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private AdminCourseEnrollmentResponse toResponse(Enrollment enrollment) {
        Progress progress = enrollment.getProgress();

        return new AdminCourseEnrollmentResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                enrollment.getStatus().name(),
                progress.getCompletedLessons(),
                progress.getTotalLessons(),
                progress.getCompletionPercentage(),
                progress.getTotalStudyMinutes(),
                enrollment.getEnrolledAt(),
                progress.getLastUpdatedAt()
        );
    }
}
