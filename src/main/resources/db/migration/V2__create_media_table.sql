CREATE TABLE media (
    id VARCHAR(36) NOT NULL,

    original_file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,

    media_type VARCHAR(20) NOT NULL,
    mime_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,

    uploaded_by VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_media
        PRIMARY KEY (id),

    CONSTRAINT uk_media_storage_key
        UNIQUE (storage_key),

    CONSTRAINT chk_media_size_bytes
        CHECK (size_bytes > 0),

    CONSTRAINT fk_media_uploaded_by
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
);

CREATE INDEX idx_media_uploaded_by
ON media(uploaded_by);