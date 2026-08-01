ALTER TABLE lesson_items
    ADD COLUMN media_id VARCHAR(36);

CREATE INDEX idx_lesson_item_media_id
    ON lesson_items(media_id);

ALTER TABLE lesson_items
    ADD CONSTRAINT fk_lesson_item_media
        FOREIGN KEY (media_id)
        REFERENCES media(id);

ALTER TABLE lesson_items
    DROP COLUMN resource_url;