package com.deutschhub.application.content.topic.service;

import com.deutschhub.application.content.shared.authorization.ContentAuthorizationPolicy;
import com.deutschhub.application.content.topic.dto.request.ReactivateTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;
import com.deutschhub.application.content.topic.port.in.ReactivateTopicUseCase;
import com.deutschhub.application.content.topic.port.out.TopicRepositoryPort;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.topic.aggregate.Topic;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ReactivateTopicService implements ReactivateTopicUseCase {

    TopicRepositoryPort topicRepositoryPort;
    CurrentActorPort currentActorPort;
    ContentAuthorizationPolicy authorizationPolicy;

    @Override
    public TopicResponse reactivate(ReactivateTopicCommand command) {

        if (command == null || command.topicId() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        authorizationPolicy.requireAdmin(actor);

        Topic topic = topicRepositoryPort.findById(command.topicId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));

        topic.reactivate();

        topicRepositoryPort.save(topic);

        return toResponse(topic);
    }

    private TopicResponse toResponse(Topic topic) {
        return new TopicResponse(topic.getId(), topic.getCategoryId(), topic.getName().value(), topic.getStatus());
    }
}