package com.deutschhub.infrastructure.content.topic.persistence.adapter;

import com.deutschhub.application.content.topic.port.out.TopicRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.topic.aggregate.Topic;
import com.deutschhub.domain.content.topic.valueobject.TopicName;
import com.deutschhub.infrastructure.content.topic.persistence.entity.TopicJpaEntity;
import com.deutschhub.infrastructure.content.topic.persistence.mapper.TopicPersistenceMapper;
import com.deutschhub.infrastructure.content.topic.persistence.repository.SpringDataTopicRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaTopicRepositoryAdapter implements TopicRepositoryPort {

    SpringDataTopicRepository springDataTopicRepository;
    TopicPersistenceMapper topicPersistenceMapper;

    @Override
    public boolean existsByName(UUID categoryId, TopicName topicName) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_CATEGORY);
        }

        if (topicName == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_NAME);
        }

        return springDataTopicRepository.existsByCategoryIdAndTopicNameNormalized(categoryId, topicName.normalizedValue());
    }

    @Override
    public void save(Topic topic) {
        if (topic == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        TopicJpaEntity entity = topicPersistenceMapper.toJpa(topic);

        springDataTopicRepository.save(entity);
    }

    @Override
    public boolean existsByNameExcludingId(UUID categoryId, TopicName topicName, UUID topicId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_CATEGORY);
        }

        if (topicId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        if (topicName == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_NAME);
        }

        return springDataTopicRepository
                .existsByCategoryIdAndTopicNameNormalizedAndIdNot(categoryId, topicName.normalizedValue(), topicId);
    }

    @Override
    public Optional<Topic> findById(UUID topicId) {
        if (topicId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        return springDataTopicRepository
                .findById(topicId)
                .map(topicPersistenceMapper::toEntity);
    }
}
