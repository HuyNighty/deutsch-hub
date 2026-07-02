package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.request.AddSectionCommand;
import com.deutschhub.application.learning.dto.response.SectionResponse;
import com.deutschhub.application.learning.port.in.AddSectionToCourseUseCase;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Section;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddSectionToCourseService implements AddSectionToCourseUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public SectionResponse addSection(AddSectionCommand command) {
        Course course = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        Section section = Section.create(command.title(), command.description(), command.orderIndex());

        course.addSection(section, command.actorId(), command.admin());
        courseRepositoryPort.save(course);
        return toResponse(section);
    }

    private SectionResponse toResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getTitle(),
                section.getDescription(),
                section.getOrderIndex(),
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }
}
