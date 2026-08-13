package com.deutschhub.domain.content.topic.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.topic.enums.TopicStatus;
import com.deutschhub.domain.content.topic.valueobject.TopicName;

import java.util.UUID;

public class Topic {

    private UUID id;
    private TopicName topicName;
    private TopicStatus topicStatus;
    private UUID categoryId;

    protected Topic() {
    }


    public static Topic create(TopicName name, UUID categoryId) {
        if (name == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_NAME);
        }

        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_CATEGORY);
        }

        Topic topic = new Topic();

        topic.id = UUID.randomUUID();
        topic.topicName = name;
        topic.categoryId = categoryId;
        topic.topicStatus = TopicStatus.ACTIVE;

        return topic;
    }

    public static Topic restore(
            UUID id,
            TopicName name,
            UUID categoryId,
            TopicStatus status
    ) {
        if (id == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_DATA);
        }

        if (name == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_NAME);
        }

        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_CATEGORY);
        }

        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_STATUS);
        }

        Topic topic = new Topic();

        topic.id = id;
        topic.topicName = name;
        topic.categoryId = categoryId;
        topic.topicStatus = status;

        return topic;
    }

    public void rename(TopicName newName) {
        if (newName == null) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_NAME);
        }

        this.topicName = newName;
    }

    public void deactivate() {
        if (topicStatus != TopicStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.TOPIC_ALREADY_INACTIVE);
        }

        this.topicStatus = TopicStatus.INACTIVE;
    }

    public void reactivate() {
        if (topicStatus != TopicStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.TOPIC_ALREADY_ACTIVE);
        }

        this.topicStatus = TopicStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public TopicName getName() {
        return topicName;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public TopicStatus getStatus() {
        return topicStatus;
    }
}
