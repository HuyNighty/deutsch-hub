package com.deutschhub.application.content.topic.port.in;

import com.deutschhub.application.content.topic.dto.request.ReactivateTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;

public interface ReactivateTopicUseCase {

    TopicResponse reactivate(ReactivateTopicCommand command);
}
