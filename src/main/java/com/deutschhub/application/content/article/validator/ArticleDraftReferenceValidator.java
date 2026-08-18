package com.deutschhub.application.content.article.validator;

import com.deutschhub.application.content.article.port.out.MediaLookupPort;
import com.deutschhub.application.content.category.port.out.CategoryRepositoryPort;
import com.deutschhub.application.content.topic.port.out.TopicRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.category.aggregate.Category;
import com.deutschhub.domain.content.category.enums.CategoryStatus;
import com.deutschhub.domain.content.topic.aggregate.Topic;
import com.deutschhub.domain.content.topic.enums.TopicStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleDraftReferenceValidator {

    CategoryRepositoryPort categoryRepositoryPort;
    TopicRepositoryPort topicRepositoryPort;
    MediaLookupPort mediaLookupPort;

    public void validate(UUID primaryCategoryId, Set<UUID> topicIds, UUID coverMediaId) {
        if (primaryCategoryId != null) {
            validateCategory(primaryCategoryId);
        }

        if (topicIds != null && !topicIds.isEmpty()) {
            validateTopics(primaryCategoryId, topicIds);
        }

        if (coverMediaId != null) {
            validateCoverMedia(coverMediaId);
        }
    }

    private void validateCategory(UUID primaryCategoryId) {

        Category category = categoryRepositoryPort.findById(primaryCategoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getStatus() != CategoryStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_ACTIVE);
        }
    }

    private void validateTopics(UUID primaryCategoryId, Set<UUID> topicIds) {
        for (UUID topicId : topicIds) {
            Topic topic = topicRepositoryPort.findById(topicId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));

            if (topic.getStatus() != TopicStatus.ACTIVE) {
                throw new BusinessException(ErrorCode.TOPIC_NOT_ACTIVE);
            }

            if (primaryCategoryId == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_ARTICLE_DATA
                );
            }

            if (!topic.getCategoryId().equals(primaryCategoryId)) {
                throw new BusinessException(ErrorCode.TOPIC_NOT_BELONG_TO_CATEGORY);
            }
        }
    }

    private void validateCoverMedia(UUID coverMediaId) {
        if (!mediaLookupPort.exists(coverMediaId)) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_FOUND);
        }

        if (!mediaLookupPort.isUsableAsArticleCover(coverMediaId)) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_SUITABLE_FOR_COVER);
        }
    }
}
