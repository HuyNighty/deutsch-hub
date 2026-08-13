package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.dto.response.LessonPreviewResponse;
import com.deutschhub.application.learning.dto.response.PublishedSectionResponse;
import com.deutschhub.application.learning.dto.response.ViewerCourseDetailResponse;
import com.deutschhub.application.learning.port.in.GetViewerCourseDetailUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.Section;
import com.deutschhub.domain.learning.model.valueobject.Progress;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetViewerCourseDetailService implements GetViewerCourseDetailUseCase {

    CourseRepositoryPort courseRepositoryPort;
    EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    public ViewerCourseDetailResponse getViewerCourseDetail(UUID courseId, UUID viewerId) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        Optional<Enrollment> enrollment = findEnrollment(viewerId, courseId);

        return toResponse(course, enrollment);
    }

    private Optional<Enrollment> findEnrollment(UUID viewerId, UUID courseId) {
        if (viewerId == null) {
            return Optional.empty();
        }

        return enrollmentRepositoryPort.findByUserIdAndCourseId(viewerId, courseId);
    }

    private ViewerCourseDetailResponse toResponse(Course course, Optional<Enrollment> enrollmentOptional) {
        List<PublishedSectionResponse> sections = course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .sorted(Comparator.comparingInt(Section::getOrderIndex))
                .map(this::toSectionResponse)
                .toList();

        Enrollment enrollment = enrollmentOptional.orElse(null);
        Progress progress = enrollment != null ? enrollment.getProgress() : null;

        return new ViewerCourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getLevel().toString(),
                course.getPrice().getAmount(),
                course.getPrice().getCurrency(),
                course.getInstructorId(),
                course.getEstimatedHours(),
                sections,
                enrollment != null,
                enrollment != null ? enrollment.getStatus().name() : null,
                progress != null ? progress.getCompletedLessons() : null,
                progress != null ? progress.getTotalLessons() : null,
                progress != null ? progress.getCompletionPercentage() : null,
                progress != null ? progress.getTotalStudyMinutes() : null,
                enrollment != null ? enrollment.getEnrolledAt() : null,
                progress != null ? progress.getLastUpdatedAt() : null,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    private PublishedSectionResponse toSectionResponse(Section section) {
        List<LessonPreviewResponse> lessons = section.getLessons()
                .stream()
                .filter(lesson -> !lesson.isDeleted())
                .sorted(Comparator.comparingInt(Lesson::getOrderIndex))
                .map(this::toLessonPreviewResponse)
                .toList();

        return new PublishedSectionResponse(
                section.getId(),
                section.getTitle(),
                section.getDescription(),
                section.getOrderIndex(),
                lessons,
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }

    private LessonPreviewResponse toLessonPreviewResponse(Lesson lesson) {
        return new LessonPreviewResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getEstimatedMinutes(),
                lesson.getLevel().toString(),
                lesson.getOrderIndex(),
                lesson.isFreePreview(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
