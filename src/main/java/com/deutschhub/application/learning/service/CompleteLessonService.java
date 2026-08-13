package com.deutschhub.application.learning.service;
import com.deutschhub.application.learning.dto.request.CompleteLessonCommand;
import com.deutschhub.application.learning.dto.response.EnrollmentProgressResponse;
import com.deutschhub.application.learning.port.in.CompleteLessonUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.application.learning.port.out.LessonCompletionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.LessonCompletion;
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
public class CompleteLessonService implements CompleteLessonUseCase {

    EnrollmentRepositoryPort enrollmentRepositoryPort;
    CourseRepositoryPort courseRepositoryPort;
    LessonCompletionRepositoryPort lessonCompletionRepositoryPort;

    @Override
    public EnrollmentProgressResponse completeLesson(CompleteLessonCommand command) {
        Enrollment enrollment = enrollmentRepositoryPort.findByUserIdAndCourseId(command.userId(), command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.isActive()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_ACTIVE);
        }

        Course course = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        Lesson lesson = findActiveLessonInCourse(course, command.lessonId());

        boolean alreadyCompleted = lessonCompletionRepositoryPort
                .existsByEnrollmentIdAndLessonId(enrollment.getId(), lesson.getId());

        if (alreadyCompleted) {
            throw new BusinessException(ErrorCode.LESSON_ALREADY_COMPLETED);
        }

        LessonCompletion lessonCompletion = LessonCompletion.create(enrollment.getId(), lesson.getId());

        lessonCompletionRepositoryPort.save(lessonCompletion);

        int completedLessons = (int) lessonCompletionRepositoryPort.countByEnrollmentId(enrollment.getId());

        int studyMinutes = Math.min(Math.max(0, command.studyMinutes()), lesson.getEstimatedMinutes());

        int totalStudyMinutes = enrollment.getProgress().getTotalStudyMinutes() +  studyMinutes;

        enrollment.updateProgress(completedLessons, totalStudyMinutes);

        Enrollment savedEnrollment = enrollmentRepositoryPort.save(enrollment);

        return toResponse(savedEnrollment);
    }

    private Lesson findActiveLessonInCourse(Course course, UUID lessonId) {
        return course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .flatMap(section -> section.getLessons().stream())
                .filter(lesson -> !lesson.isDeleted())
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
    }

    private EnrollmentProgressResponse toResponse(Enrollment enrollment) {
        Progress progress = enrollment.getProgress();

        return new EnrollmentProgressResponse(enrollment.getId(), enrollment.getCourseId(), enrollment.getStatus().name(),
                progress.getCompletedLessons(), progress.getTotalLessons(), progress.getCompletionPercentage(),
                progress.getTotalStudyMinutes(), progress.getLastUpdatedAt());
    }
}
