package com.deutschhub.application.content.topic.port.out;

import com.deutschhub.domain.content.topic.aggregate.Topic;
import com.deutschhub.domain.content.topic.valueobject.TopicName;

import java.util.Optional;
import java.util.UUID;

public interface TopicRepositoryPort {

    boolean existsByName(UUID categoryId, TopicName topicName);

    void save(Topic topic);

    boolean existsByNameExcludingId(UUID categoryId, TopicName topicName, UUID topicId);

    Optional<Topic> findById(UUID topicId);

}
