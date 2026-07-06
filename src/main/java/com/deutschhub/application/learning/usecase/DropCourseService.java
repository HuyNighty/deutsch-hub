package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.port.in.DropCourseUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.valueobject.EnrollmentStatus;
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
public class DropCourseService implements DropCourseUseCase {

    EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    public void dropCourse(UUID userId, UUID courseId) {
        Enrollment enrollment = enrollmentRepositoryPort.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        enrollment.drop();

        enrollmentRepositoryPort.save(enrollment);
    }
}
