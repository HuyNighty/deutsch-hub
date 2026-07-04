package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.LessonResponse;
import com.deutschhub.application.learning.dto.response.MyCourseDetailResponse;
import com.deutschhub.application.learning.dto.response.SectionDetailResponse;
import com.deutschhub.application.learning.port.in.GetMyCourseDetailUseCase;
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class GetMyCourseDetailService implements GetMyCourseDetailUseCase{

    CourseRepositoryPort courseRepositoryPort;
    EnrollmentRepositoryPort enrollmentRepositoryPort;

    @Override
    public MyCourseDetailResponse getMyCourseDetail(UUID userId, UUID courseId) {
        Enrollment enrollment = enrollmentRepositoryPort.findByUserIdAndCourseId(userId,courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.isActive()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_ACTIVE);
        }

        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        return toResponse(course, enrollment);
    }

    private MyCourseDetailResponse toResponse(Course course, Enrollment enrollment) {
        Progress progress = enrollment.getProgress();

        List<SectionDetailResponse> sections = course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .sorted(Comparator.comparingInt(Section::getOrderIndex))
                .map(this::toDetailResponse)
                .toList();

        return new MyCourseDetailResponse(
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
                sections,
                enrollment.getEnrolledAt(),
                progress.getLastUpdatedAt()
        );
    }

    private SectionDetailResponse toDetailResponse(Section section) {

        List<LessonResponse> lessons = section.getLessons()
                .stream()
                .filter(lesson -> !lesson.isDeleted())
                .sorted(Comparator.comparingInt(Lesson::getOrderIndex))
                .map(this::toLessonResponse)
                .toList();

        return new SectionDetailResponse(
                section.getId(),
                section.getTitle(),
                section.getDescription(),
                section.getOrderIndex(),
                lessons,
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getContent(),
                lesson.getEstimatedMinutes(),
                lesson.getLevel().toString(),
                lesson.getOrderIndex(),
                lesson.isFreePreview(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
