package com.deutschhub.infrastructure.content.topic.persistence.mapper;

import com.deutschhub.domain.content.topic.aggregate.Topic;
import com.deutschhub.domain.content.topic.enums.TopicStatus;
import com.deutschhub.domain.content.topic.valueobject.TopicName;
import com.deutschhub.infrastructure.content.topic.persistence.entity.TopicJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TopicPersistenceMapper {

    public TopicJpaEntity toJpa(Topic topic) {
        if (topic == null) {
            return null;
        }

        return TopicJpaEntity
                .builder()
                .id(topic.getId())
                .categoryId(topic.getCategoryId())
                .topicName(topic.getName().value())
                .topicNameNormalized(topic.getName().normalizedValue())
                .topicStatus(topic.getStatus().name())
                .build();
    }

    public Topic toEntity(TopicJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Topic.restore(
                entity.getId(),
                new TopicName(entity.getTopicName()),
                entity.getCategoryId(),
                TopicStatus.valueOf(entity.getTopicStatus())
        );
    }
}
