ALTER TABLE articles
    ADD COLUMN lock_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE article_versions
    ADD COLUMN lock_version BIGINT NOT NULL DEFAULT 0;