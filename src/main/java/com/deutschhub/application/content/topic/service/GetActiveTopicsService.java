package com.deutschhub.application.content.topic.service;

import com.deutschhub.application.content.category.port.out.CategoryQueryPort;
import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;
import com.deutschhub.application.content.topic.port.in.GetActiveTopicsUseCase;
import com.deutschhub.application.content.topic.port.out.TopicQueryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class GetActiveTopicsService implements GetActiveTopicsUseCase {

    TopicQueryPort topicQueryPort;
    CategoryQueryPort categoryQueryPort;

    @Override
    public List<TopicSummaryResponse> getActiveTopicsByCategoryId(UUID categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_CATEGORY);
        }

        if (!categoryQueryPort.existsActiveCategory(categoryId)) {
            return List.of();
        }

        return topicQueryPort.findActiveTopicsByCategoryId(categoryId);
    }
}