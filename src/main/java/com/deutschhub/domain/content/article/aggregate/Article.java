package com.deutschhub.domain.content.article.aggregate;

import com.deutschhub.domain.content.article.entity.ArticleVersion;
import com.deutschhub.domain.content.article.entity.ReviewCycle;
import com.deutschhub.domain.content.article.enums.EditorialStatus;
import com.deutschhub.domain.content.article.enums.PublicationStatus;
import com.deutschhub.domain.content.article.service.SlugGenerator;
import com.deutschhub.domain.content.article.valueobject.Slug;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.time.Instant;
import java.util.*;

public class Article {

    private UUID id;
    private UserId ownerId;

    private Slug slug;

    private EditorialStatus editorialStatus;
    private PublicationStatus publicationStatus;

    private UUID draftVersionId;
    private UUID publishedVersionId;

    private List<ArticleVersion> versions;
    private List<ReviewCycle> reviewHistory;

    private Instant createdAt;
    private UserId createdBy;

    private Instant publishedAt;
    private UserId publishedBy;

    private Instant archivedAt;
    private UserId archivedBy;

    public static Article createDraft(UserId owner, Slug slug, Instant createdAt) {
        Article article = new Article();

        article.id = UUID.randomUUID();

        article.slug = slug;

        article.ownerId = owner;

        article.editorialStatus = EditorialStatus.DRAFT;

        article.publicationStatus = PublicationStatus.UNPUBLISHED;

        article.createdAt = createdAt;
        article.createdBy = owner;

        article.versions = new ArrayList<>();
        article.reviewHistory = new ArrayList<>();

        ArticleVersion draft = ArticleVersion.createFirstDraft(UUID.randomUUID(), owner, createdAt);

        article.draftVersionId = draft.getId();

        article.versions.add(draft);

        return article;
    }
}

