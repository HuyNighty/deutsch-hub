package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.dto.response.EnrollmentResponse;
import com.deutschhub.application.learning.port.in.EnrollCourseUseCase;
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

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EnrollCourseService implements EnrollCourseUseCase {

    CourseRepositoryPort courseRepositoryPort;
    EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    public EnrollmentResponse enroll(UUID userId, UUID courseId) {
        Course course = courseRepositoryPort.findPublishedById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.isPublished()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        boolean alreadyEnrolled = enrollmentRepositoryPort.existsByUserIdAndCourseId(userId, courseId);

        if (alreadyEnrolled) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_ENROLLED);
        }

        int totalLessons = countActiveLessons(course);

        Enrollment enrollment = Enrollment.create(userId, courseId, totalLessons);

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        return toResponse(savedEnrollment);
    }

    private int countActiveLessons(Course course) {
        return (int) course.getSections().stream()
                .filter(section -> !section.isDeleted())
                .flatMap(section -> section.getLessons().stream())
                .filter(lesson -> !lesson.isDeleted())
                .count();
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        Progress progress = enrollment.getProgress();

        return new EnrollmentResponse( enrollment.getId(), enrollment.getUserId(), enrollment.getCourseId(),
                enrollment.getStatus().name(), progress.getCompletedLessons(), progress.getTotalLessons(),
                progress.getCompletionPercentage(), progress.getTotalStudyMinutes(), enrollment.getEnrolledAt());
    }
}
