package com.deutschhub.infrastructure.content.topic.persistence.adapter;

import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;
import com.deutschhub.application.content.topic.port.out.TopicQueryPort;
import com.deutschhub.domain.content.topic.enums.TopicStatus;
import com.deutschhub.infrastructure.content.topic.persistence.entity.TopicJpaEntity;
import com.deutschhub.infrastructure.content.topic.persistence.repository.SpringDataTopicRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaTopicQueryAdapter implements TopicQueryPort {

    SpringDataTopicRepository springDataTopicRepository;

    @Override
    public List<TopicSummaryResponse> findActiveTopicsByCategoryId(UUID categoryId) {
        if (categoryId == null) {
            return List.of();
        }

        return springDataTopicRepository.findByCategoryIdAndTopicStatus(categoryId, TopicStatus.ACTIVE.name())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TopicSummaryResponse toResponse(TopicJpaEntity topic) {
        return new TopicSummaryResponse(topic.getId(), topic.getTopicName(), topic.getCategoryId());
    }
}
