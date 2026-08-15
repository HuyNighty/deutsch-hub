package com.deutschhub.application.content.topic.port.in;

import com.deutschhub.application.content.topic.dto.request.CreateTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;

public interface CreateTopicUseCase {

    TopicResponse create(CreateTopicCommand command);

}
