package com.deutschhub.application.content.topic.port.in;

import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface GetActiveTopicsUseCase {

    List<TopicSummaryResponse> getActiveTopicsByCategoryId(UUID categoryId);
}
