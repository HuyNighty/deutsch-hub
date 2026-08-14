-- DeutschHub
-- Version 4
-- Initial schema

-- CONTENT CONTEXT V4

CREATE TABLE categories (
    id VARCHAR(36) NOT NULL,

    category_name VARCHAR(100) NOT NULL,
    category_name_normalized VARCHAR(100) NOT NULL,
    category_status VARCHAR(20) NOT NULL,

    CONSTRAINT pk_categories
        PRIMARY KEY (id),

    CONSTRAINT uk_categories_name_normalized
        UNIQUE (category_name_normalized),

    CONSTRAINT chk_categories_status
        CHECK (category_status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE topics (
    id VARCHAR(36) NOT NULL,

    category_id VARCHAR(36) NOT NULL,

    topic_name VARCHAR(100) NOT NULL,
    topic_name_normalized VARCHAR(100) NOT NULL,
    topic_status VARCHAR(20) NOT NULL,

    CONSTRAINT pk_topics
        PRIMARY KEY (id),

    CONSTRAINT fk_topics_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id),

    CONSTRAINT uk_topics_category_name_normalized
        UNIQUE (category_id, topic_name_normalized),

    CONSTRAINT chk_topics_status
        CHECK (topic_status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE articles (
    id VARCHAR(36) NOT NULL,

    owner_id VARCHAR(36) NOT NULL,

    slug VARCHAR(255) NOT NULL,

    editorial_status VARCHAR(30) NOT NULL,
    publication_status VARCHAR(30) NOT NULL,

    draft_version_id VARCHAR(36),
    published_version_id VARCHAR(36),

    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(36) NOT NULL,

    published_at DATETIME(6),
    published_by VARCHAR(36),

    archived_at DATETIME(6),
    archived_by VARCHAR(36),

    ownership_transferred_at DATETIME(6),
    ownership_transferred_by VARCHAR(36),

    CONSTRAINT pk_articles
        PRIMARY KEY (id),

    CONSTRAINT fk_articles_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id),

    CONSTRAINT fk_article_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),

    CONSTRAINT fk_articles_published_by
        FOREIGN KEY (published_by)
        REFERENCES users(id),

    CONSTRAINT fk_articles_archived_by
        FOREIGN KEY (archived_by)
        REFERENCES users(id),

    CONSTRAINT fk_articles_ownership_transferred_by
        FOREIGN KEY (ownership_transferred_by)
        REFERENCES users(id),

    CONSTRAINT uk_articles_slug
        UNIQUE (slug),

    CONSTRAINT chk_articles_editorial_status
        CHECK (editorial_status IN (
                'DRAFT',
                'IN_REVIEW',
                'CHANGES_REQUESTED',
                'IDLE'
            )
        ),

    CONSTRAINT chk_articles_publication_status
        CHECK (publication_status IN (
                'UNPUBLISHED',
                'PUBLISHED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT chk_articles_published_audit
        CHECK (
            (published_at IS NULL AND published_by IS NULL)
            OR
             (published_at IS NOT NULL AND published_by IS NOT NULL)
        ),

    CONSTRAINT chk_articles_archived_audit
        CHECK (
            (archived_at IS NULL AND archived_by IS NULL)
            OR
            (archived_at IS NOT NULL AND archived_by IS NOT NULL)
        ),

    CONSTRAINT chk_articles_ownership_transfer_audit
        CHECK (
            (ownership_transferred_at IS NULL AND ownership_transferred_by IS NULL)
            OR
            (ownership_transferred_at IS NOT NULL AND ownership_transferred_by IS NOT NULL)
        )
);

CREATE TABLE article_versions (
    id VARCHAR(36) NOT NULL,

    article_id VARCHAR(36) NOT NULL,

    version_number INT NOT NULL,

    title VARCHAR(255) NOT NULL,
    summary VARCHAR(2000),
    body MEDIUMTEXT,

    primary_category_id VARCHAR(36),
    cover_media_id VARCHAR(36),

    created_by VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,

    last_modified_by VARCHAR(36) NOT NULL,
    last_modified_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_article_versions
        PRIMARY KEY (id),

    CONSTRAINT fk_article_versions_article
        FOREIGN KEY (article_id)
        REFERENCES articles(id),

    CONSTRAINT fk_article_versions_primary_category
        FOREIGN KEY (primary_category_id)
        REFERENCES categories(id),

    CONSTRAINT fk_article_versions_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),

    CONSTRAINT fk_article_versions_last_modified_by
            FOREIGN KEY (last_modified_by)
            REFERENCES users(id),

    CONSTRAINT fk_article_versions_cover_media
        FOREIGN KEY (cover_media_id)
        REFERENCES media(id),

    CONSTRAINT uk_article_versions_article_version
        UNIQUE (article_id, version_number),

    CONSTRAINT uk_article_versions_id_article
        UNIQUE (id, article_id),

    CONSTRAINT chk_article_versions_number
        CHECK (version_number >= 1)
);

ALTER TABLE articles
    ADD CONSTRAINT fk_articles_draft_version
        FOREIGN KEY (draft_version_id, id)
        REFERENCES article_versions(id, article_id),

    ADD CONSTRAINT fk_articles_published_version
        FOREIGN KEY (published_version_id, id)
        REFERENCES article_versions(id, article_id);

CREATE TABLE review_cycles (
    id VARCHAR(36) NOT NULL,

    article_version_id VARCHAR(36) NOT NULL,

    submitted_by VARCHAR(36) NOT NULL,
    submitted_at DATETIME(6) NOT NULL,

    reviewed_by VARCHAR(36),
    reviewed_at DATETIME(6),

    withdrawn_by VARCHAR(36),
    withdrawn_at DATETIME(6),

    result VARCHAR(30) NOT NULL,
    feedback TEXT,

    CONSTRAINT pk_review_cycles
        PRIMARY KEY (id),

    CONSTRAINT fk_review_cycles_article_version
        FOREIGN KEY (article_version_id)
        REFERENCES article_versions(id),

    CONSTRAINT fk_review_cycles_submitted_by
        FOREIGN KEY (submitted_by)
        REFERENCES users(id),

    CONSTRAINT fk_review_cycles_reviewed_by
        FOREIGN KEY (reviewed_by)
        REFERENCES users(id),

    CONSTRAINT fk_review_cycles_withdrawn_by
        FOREIGN KEY (withdrawn_by)
        REFERENCES users(id),

    CONSTRAINT chk_review_cycles
        CHECK (
            (
                result = 'PENDING'
                AND reviewed_by IS NULL
                AND reviewed_at IS NULL
                AND withdrawn_by IS NULL
                AND withdrawn_at IS NULL
                AND feedback IS NULL
            )
            OR
            (
                result = 'APPROVED'
                AND reviewed_by IS NOT NULL
                AND reviewed_at IS NOT NULL
                AND withdrawn_by IS NULL
                AND withdrawn_at IS NULL
                AND feedback IS NULL
            )
            OR
            (
                result = 'CHANGES_REQUESTED'
                AND reviewed_by IS NOT NULL
                AND reviewed_at IS NOT NULL
                AND withdrawn_by IS NULL
                AND withdrawn_at IS NULL
                AND feedback IS NOT NULL
            )
            OR
            (
                result = 'WITHDRAWN'
                AND reviewed_by IS NULL
                AND reviewed_at IS NULL
                AND withdrawn_by IS NOT NULL
                AND withdrawn_at IS NOT NULL
                AND feedback IS NULL
            )
        )
);

CREATE TABLE article_version_topics (
    article_version_id VARCHAR(36) NOT NULL,
    topic_id VARCHAR(36) NOT NULL,

    CONSTRAINT pk_article_version_topics
        PRIMARY KEY (article_version_id, topic_id),

    CONSTRAINT fk_article_version_topics_version
        FOREIGN KEY (article_version_id)
        REFERENCES article_versions(id),

    CONSTRAINT fk_article_version_topics_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics(id)
);

CREATE TABLE article_version_sources (
    article_version_id VARCHAR(36) NOT NULL,

    source_order INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    url VARCHAR(2048) NOT NULL,

    CONSTRAINT pk_article_version_sources
    PRIMARY KEY (article_version_id, source_order),

    CONSTRAINT fk_article_version_sources_version
        FOREIGN KEY (article_version_id)
        REFERENCES article_versions(id),

    CONSTRAINT chk_article_version_sources_source_order
        CHECK (source_order >= 0)
);

CREATE INDEX idx_articles_owner_id
ON articles(owner_id);

CREATE INDEX idx_article_versions_primary_category_id
ON article_versions(primary_category_id);

CREATE INDEX idx_review_cycles_article_version_id
ON review_cycles(article_version_id);

CREATE INDEX idx_article_version_topics_topic_id
ON article_version_topics(topic_id);