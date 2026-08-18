package com.deutschhub.application.content.topic.service;

import com.deutschhub.application.content.shared.authorization.ContentAuthorizationPolicy;
import com.deutschhub.application.content.category.port.out.CategoryRepositoryPort;
import com.deutschhub.application.content.topic.dto.request.CreateTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;
import com.deutschhub.application.content.topic.port.in.CreateTopicUseCase;
import com.deutschhub.application.content.topic.port.out.TopicRepositoryPort;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.topic.aggregate.Topic;
import com.deutschhub.domain.content.topic.valueobject.TopicName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CreateTopicService implements CreateTopicUseCase {

    TopicRepositoryPort topicRepositoryPort;
    CategoryRepositoryPort categoryRepositoryPort;
    CurrentActorPort currentActorPort;
    ContentAuthorizationPolicy authorizationPolicy;

    @Override
    public TopicResponse create(CreateTopicCommand command) {
        if (command == null || command.categoryId() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        authorizationPolicy.requireAdmin(actor);

        categoryRepositoryPort.findById(command.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        TopicName topicName = new TopicName(command.name());

        if (topicRepositoryPort.existsByName(command.categoryId(), topicName)) {
            throw new BusinessException(ErrorCode.TOPIC_NAME_ALREADY_EXISTS);
        }

        Topic topic = Topic.create(topicName, command.categoryId());

        topicRepositoryPort.save(topic);

        return toDomain(topic);
    }

    private TopicResponse toDomain(Topic topic) {
        return new TopicResponse(topic.getId(), topic.getCategoryId(), topic.getName().value(), topic.getStatus());
    }
}
