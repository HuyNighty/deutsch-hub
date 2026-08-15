package com.deutschhub.application.content.topic.port.in;

import com.deutschhub.application.content.topic.dto.request.RenameTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;

public interface RenameTopicUseCase {

    TopicResponse rename(RenameTopicCommand command);

}
