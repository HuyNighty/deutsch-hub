package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.SectionResponse;
import com.deutschhub.application.learning.port.in.GetCourseSectionUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Section;
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
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetCourseSectionService implements GetCourseSectionUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public List<SectionResponse> getSections(UUID courseId) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        return course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .sorted(Comparator.comparingInt(Section::getOrderIndex))
                .map(this::toResponse)
                .toList();
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
