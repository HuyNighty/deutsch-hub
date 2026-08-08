package com.deutschhub.domain.content.article.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.valueobject.*;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ArticleVersion {

    private UUID id;
    private VersionNumber versionNumber;

    private ArticleTitle title;
    private Summary summary;
    private Body body;

    private UUID primaryCategoryId;
    private List<UUID> topicIds;

    private UUID coverMediaId;

    private List<Source> sources;

    private UserId createdBy;
    private Instant createdAt;

    private UserId lastModifiedBy;
    private Instant lastModifiedAt;

    protected ArticleVersion() {}

    public ArticleVersion(UUID id, VersionNumber versionNumber, ArticleTitle title, Summary summary, Body body,
                          UUID primaryCategoryId, List<UUID> topicIds, UUID coverMediaId, List<Source> sources,
                          Instant createdAt, UserId createdBy) {

        if (topicIds == null || sources == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_COLLECTION);
        }

        this.id = id;
        this.versionNumber = versionNumber;

        this.title = title;
        this.summary = summary;
        this.body = body;

        this.primaryCategoryId = primaryCategoryId;
        this.topicIds = List.copyOf(topicIds);

        this.coverMediaId = coverMediaId;

        this.sources = List.copyOf(sources);

        this.createdAt = createdAt;
        this.createdBy = createdBy;

        this.lastModifiedAt = createdAt;
        this.lastModifiedBy = createdBy;
    }

    public void updateContent(ArticleTitle title, Summary summary, Body body, UUID primaryCategoryId, List<UUID> topicIds,
                              UUID coverMediaId, List<Source> sources, UserId modifiedBy, Instant modifiedAt) {

        if (topicIds == null || sources == null || modifiedBy == null || modifiedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_COLLECTION);
        }

        this.title = title;
        this.summary = summary;
        this.body = body;

        this.primaryCategoryId = primaryCategoryId;
        this.topicIds = List.copyOf(topicIds);

        this.coverMediaId = coverMediaId;

        this.sources = List.copyOf(sources);

        this.lastModifiedBy = modifiedBy;
        this.lastModifiedAt = modifiedAt;
    }

    public static ArticleVersion createFirstDraft(UUID id, UserId createdBy, Instant createdAt) {
        return new ArticleVersion(id, VersionNumber.first(), null, null, null, null,
                List.of(), null, List.of(), createdAt, createdBy);
    }

    public UUID getId() {
        return id;
    }

    public VersionNumber getVersionNumber() {
        return versionNumber;
    }

    public ArticleTitle getTitle() {
        return title;
    }

    public Summary getSummary() {
        return summary;
    }

    public Body getBody() {
        return body;
    }

    public UUID getPrimaryCategoryId() {
        return primaryCategoryId;
    }

    public List<UUID> getTopicIds() {
        return topicIds;
    }

    public UUID getCoverMediaId() {
        return coverMediaId;
    }

    public List<Source> getSources() {
        return sources;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserId getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }
}
