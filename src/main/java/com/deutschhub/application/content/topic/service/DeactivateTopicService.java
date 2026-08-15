package com.deutschhub.application.content.topic.service;

import com.deutschhub.application.content.topic.dto.request.DeactivateTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;
import com.deutschhub.application.content.topic.port.in.DeactivateTopicUseCase;
import com.deutschhub.application.content.topic.port.out.TopicRepositoryPort;
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
public class DeactivateTopicService implements DeactivateTopicUseCase {

    TopicRepositoryPort topicRepositoryPort;

    @Override
    public TopicResponse deactivate(DeactivateTopicCommand command) {

        if (command == null || command.topicId() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        Topic topic = topicRepositoryPort.findById(command.topicId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));

        topic.deactivate();

        topicRepositoryPort.save(topic);

        return toResponse(topic);
    }

    private TopicResponse toResponse(Topic topic) {
        return new TopicResponse(topic.getId(), topic.getCategoryId(), topic.getName().value(), topic.getStatus());
    }
}