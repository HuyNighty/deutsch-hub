package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.MyCourseResponse;
import com.deutschhub.application.learning.port.in.GetMyCoursesUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMyCoursesService implements GetMyCoursesUseCase {

    EnrollmentRepositoryPort enrollmentRepositoryPort;
    CourseRepositoryPort courseRepositoryPort;

    @Override
    public List<MyCourseResponse> getMyCourses(UUID userId) {
        return enrollmentRepositoryPort.findByUserId(userId)
                .stream()
                .filter(Enrollment::isActive)
                .sorted(Comparator.comparing(Enrollment::getEnrolledAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private MyCourseResponse toResponse(Enrollment enrollment) {
        Course course = courseRepositoryPort.findById(enrollment.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        Progress progress = enrollment.getProgress();

        return new MyCourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getLevel().toString(),
                course.getPrice().getAmount(),
                course.getPrice().getCurrency(),
                course.getEstimatedHours(),
                enrollment.getStatus().name(),
                progress.getCompletedLessons(),
                progress.getTotalLessons(),
                progress.getCompletionPercentage(),
                progress.getTotalStudyMinutes(),
                enrollment.getEnrolledAt(),
                progress.getLastUpdatedAt());
    }
}
