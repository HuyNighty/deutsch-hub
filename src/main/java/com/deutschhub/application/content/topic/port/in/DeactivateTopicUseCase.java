package com.deutschhub.application.content.topic.port.in;

import com.deutschhub.application.content.topic.dto.request.DeactivateTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;

public interface DeactivateTopicUseCase {
    TopicResponse deactivate(DeactivateTopicCommand command);
}
