package com.deutschhub.application.content.topic.service;

import com.deutschhub.application.content.topic.dto.request.RenameTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;
import com.deutschhub.application.content.topic.port.in.RenameTopicUseCase;
import com.deutschhub.application.content.topic.port.out.TopicRepositoryPort;
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
public class RenameTopicService implements RenameTopicUseCase {

    TopicRepositoryPort topicRepositoryPort;

    @Override
    public TopicResponse rename(RenameTopicCommand command) {
        if (command == null || command.topicId() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        Topic topic = topicRepositoryPort.findById(command.topicId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));

        TopicName newName = new TopicName(command.name());

        if (topic.getName().normalizedValue().equals(newName.normalizedValue())) {
            return toResponse(topic);
        }

        if (topicRepositoryPort.existsByNameExcludingId(topic.getCategoryId(), newName, topic.getId())) {
            throw new BusinessException(ErrorCode.TOPIC_NAME_ALREADY_EXITS);
        }

        topic.rename(newName);

        topicRepositoryPort.save(topic);

        return toResponse(topic);
    }

    private TopicResponse toResponse(Topic topic) {
        return new TopicResponse(topic.getId(), topic.getCategoryId(), topic.getName().value(), topic.getStatus());
    }
}
