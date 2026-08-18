package com.deutschhub.application.content.topic.port.out;

import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface TopicQueryPort {

    List<TopicSummaryResponse> findActiveTopicsByCategoryId(UUID categoryId);
}
